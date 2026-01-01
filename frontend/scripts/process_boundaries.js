
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import * as turf from '@turf/turf';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const INPUT_FILE = path.join(__dirname, '../public/shp.geojson');
const OUTPUT_FILE = path.join(__dirname, '../public/region_hierarchy.json');

console.log('Reading shp.geojson...');
const rawData = fs.readFileSync(INPUT_FILE, 'utf8');
const geojson = JSON.parse(rawData);

console.log(`Loaded ${geojson.features.length} features.`);

// Data structures
const countyMap = new Map(); // Name -> Feature
const cityMap = new Map();   // Name -> Feature
const provinceMap = new Map(); // Name -> Feature

// Grouping by raw features first
const countyFeatures = {};
const cityFeatures = {};
const provinceFeatures = [];

// Helper to add feature to group
function addToGroup(group, key, feature) {
    if (!group[key]) {
        group[key] = [];
    }
    group[key].push(feature);
}

// 1. Group Raw Features
geojson.features.forEach(feature => {
    const props = feature.properties;
    if (!props) return;

    const county = (props.COUNTY || props.county || '').trim();
    const city = (props.CITY || props.city || '').trim();
    const province = (props.PROVINCE || props.province || '四川省').trim();

    if (county) addToGroup(countyFeatures, county, feature);
    if (city) addToGroup(cityFeatures, city, feature);
    if (province) provinceFeatures.push(feature);
});

// Helper: Union and Simplify
function processFeatures(features, name, level, tolerance = 0.001) {
    if (!features || features.length === 0) return null;

    let merged = null;
    try {
        const fc = turf.featureCollection(features);
        try {
             merged = turf.union(fc);
        } catch (e) {
            console.warn(`  Standard union failed for ${name}, trying iterative...`);
            merged = features[0];
            for (let i = 1; i < features.length; i++) {
                merged = turf.union(turf.featureCollection([merged, features[i]]));
            }
        }
    } catch (err) {
        console.error(`  Union failed completely for ${name}`, err);
        return null;
    }

    if (!merged) return null;

    // Simplification to reduce coordinate count
    // Target: ~200 points. 
    // We start with a tolerance and adjust if needed.
    let currentTolerance = tolerance;
    let simplified = turf.simplify(merged, { tolerance: currentTolerance, highQuality: true });
    
    // Check coordinate count (approximate by string length or counting)
    // A simple count of coordinates in the first ring of the first polygon
    let coordCount = 0;
    const countCoords = (geom) => {
        let count = 0;
        if (geom.type === 'Polygon') {
            geom.coordinates.forEach(ring => count += ring.length);
        } else if (geom.type === 'MultiPolygon') {
            geom.coordinates.forEach(poly => {
                poly.forEach(ring => count += ring.length);
            });
        }
        return count;
    };
    
    coordCount = countCoords(simplified.geometry);

    // Heuristic: if > 200 points, increase tolerance
    // We want to be aggressive to meet the user's "200 coordinate pairs" requirement for fast rendering
    let attempts = 0;
    while (coordCount > 200 && attempts < 10) {
        currentTolerance *= 1.5;
        simplified = turf.simplify(merged, { tolerance: currentTolerance, highQuality: false }); // highQuality: false for speed/aggression
        coordCount = countCoords(simplified.geometry);
        attempts++;
    }
    
    console.log(`  ${name} simplified to ${coordCount} points (tol: ${currentTolerance.toFixed(5)})`);

    // Add properties
    simplified.properties = {
        name: name,
        level: level,
        childCount: features.length
    };
    
    return simplified;
}

// 2. Process Hierarchies
// We want a strictly nested structure: Province -> City -> County
const treeRoot = {
    name: "四川省",
    level: "province",
    code: "510000",
    children: [], // Will contain City nodes
    geometry: null,
    bbox: null,
    center: null
};

// Map for temporary lookup to build the tree
const cityNodes = {}; // name -> node

// --- Process Province ---
console.log('Processing Province...');
const provFeature = processFeatures(provinceFeatures, '四川省', 'province', 0.005); // Start with higher tolerance for province
if (provFeature) {
    const bbox = turf.bbox(provFeature);
    const center = turf.centerOfMass(provFeature).geometry.coordinates;
    treeRoot.geometry = provFeature.geometry;
    treeRoot.bbox = bbox;
    treeRoot.center = center;
}

// --- Process Cities ---
const cityKeys = Object.keys(cityFeatures);
console.log(`Processing ${cityKeys.length} Cities...`);
cityKeys.forEach(cityName => {
    const feats = cityFeatures[cityName];
    const cityFeat = processFeatures(feats, cityName, 'city', 0.002);
    
    if (cityFeat) {
        const bbox = turf.bbox(cityFeat);
        const center = turf.centerOfMass(cityFeat).geometry.coordinates;
        
        const cityNode = {
            name: cityName,
            level: 'city',
            children: [], // Will contain County nodes
            geometry: cityFeat.geometry,
            bbox: bbox,
            center: center
        };
        
        treeRoot.children.push(cityNode);
        cityNodes[cityName] = cityNode;
    }
});

// --- Process Counties ---
const countyKeys = Object.keys(countyFeatures);
console.log(`Processing ${countyKeys.length} Counties...`);
countyKeys.forEach(countyName => {
    const feats = countyFeatures[countyName];
    // Find parent city
    const sampleProps = feats[0].properties;
    const parentCityName = (sampleProps.CITY || sampleProps.city || '').trim();
    
    const countyFeat = processFeatures(feats, countyName, 'county', 0.001);
    
    if (countyFeat) {
        const bbox = turf.bbox(countyFeat);
        const center = turf.centerOfMass(countyFeat).geometry.coordinates;
        
        const countyNode = {
            name: countyName,
            level: 'county',
            // Counties are leaves in this view (townships merged into county)
            geometry: countyFeat.geometry,
            bbox: bbox,
            center: center
        };
        
        // Link to parent city
        if (parentCityName && cityNodes[parentCityName]) {
            cityNodes[parentCityName].children.push(countyNode);
        } else {
             console.warn(`  Orphan county: ${countyName} (City: ${parentCityName})`);
             // Optional: Add to province directly if city missing? 
             // For now, we strictly follow the hierarchy. If city is missing, it might be an error in data.
        }
    }
});

// 3. Write Output
// The user explicitly requested: "市里面放省里面，县放在市里面"
// We export an object keyed by the province name to be clear.
const outputData = {
    "四川省": treeRoot
};

console.log('Saving hierarchy...');
fs.writeFileSync(OUTPUT_FILE, JSON.stringify(outputData, null, 2));

console.log('Done.');
