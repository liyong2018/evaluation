
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { spawnSync } from 'child_process';
import crypto from 'crypto';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const INPUT_FILE = path.join(__dirname, '../public/shp.geojson');
const OUTPUT_FILE = path.join(__dirname, '../public/region_hierarchy.json');
const VILLAGE_POINTS_FILE = path.join(__dirname, '../public/village_points/village_points.geojson');
const OUTPUT_TXT_FILE = path.join(__dirname, '../public/sichuan_province_city_county.txt');
const OUTPUT_COUNTY_TOWNSHIP_COMMUNITY_RELATION_FILE = path.join(__dirname, '../public/sichuan_county_township_community_relation.txt');
const SICHUAN_ORG_TXT_FILE = path.join(__dirname, '../public/sichuan_province_city_county.txt');
const SICHUAN_COUNTY_TOWNSHIP_COMMUNITY_RELATION_TXT_FILE = path.join(__dirname, '../public/sichuan_county_township_community_relation.txt');

function extractCityCountyTreeFromShp(geojson) {
    const countiesByCity = new Map();
    const features = Array.isArray(geojson?.features) ? geojson.features : [];
    for (const feature of features) {
        const props = feature?.properties;
        if (!props) continue;
        const city = String(props.CITY || props.city || '').trim();
        const county = String(props.COUNTY || props.county || '').trim();
        if (!city || !county) continue;
        if (!countiesByCity.has(city)) {
            countiesByCity.set(city, new Set());
        }
        countiesByCity.get(city).add(county);
    }
    return countiesByCity;
}

function extractCodesFromVillagePoints(pointsGeojson) {
    const cityCodeByName = new Map();
    const countyCodeByCityCounty = new Map();
    const countyCodeByName = new Map();

    const features = Array.isArray(pointsGeojson?.features) ? pointsGeojson.features : [];
    for (const feature of features) {
        const props = feature?.properties;
        if (!props) continue;

        const code = String(props.code || '').trim();
        if (code.length < 6) continue;

        const city = String(props.dzshi || '').trim();
        const county = String(props.dzxian || '').trim();

        if (city && code.length >= 4) {
            const cityCode = code.slice(0, 4);
            if (!cityCodeByName.has(city)) {
                cityCodeByName.set(city, cityCode);
            }
        }

        if (city && county) {
            const countyCode = code.slice(0, 6);
            const key = `${city}||${county}`;
            if (!countyCodeByCityCounty.has(key)) {
                countyCodeByCityCounty.set(key, countyCode);
            }
            if (!countyCodeByName.has(county)) {
                countyCodeByName.set(county, countyCode);
            }
        }
    }

    return { cityCodeByName, countyCodeByCityCounty, countyCodeByName };
}

function extractCountyTownshipPairsFromShp(geojson) {
    const pairs = new Map();
    const features = Array.isArray(geojson?.features) ? geojson.features : [];
    for (const feature of features) {
        const props = feature?.properties;
        if (!props) continue;
        const city = String(props.CITY || props.city || '').trim();
        const county = String(props.COUNTY || props.county || '').trim();
        const township = String(props.xiang || props.XIANG || props.township || '').trim();
        if (!county || !township) continue;
        const key = `${city}||${county}||${township}`;
        if (!pairs.has(key)) {
            pairs.set(key, { city, county, township });
        }
    }
    return [...pairs.values()];
}

function buildPointsIndexByCounty(pointsGeojson) {
    const pointsByCounty = new Map();
    const features = Array.isArray(pointsGeojson?.features) ? pointsGeojson.features : [];
    for (const feature of features) {
        const props = feature?.properties;
        if (!props) continue;
        const county = String(props.dzxian || '').trim();
        const address = String(props.address || '').trim();
        const code = String(props.code || '').trim();
        if (!county || !code) continue;
        if (!pointsByCounty.has(county)) pointsByCounty.set(county, []);
        pointsByCounty.get(county).push({ address, code });
    }
    return pointsByCounty;
}

function extractTownshipNameFromAddress(addressRaw, countyName) {
    const address = String(addressRaw || '').replace(/\s+/g, '');
    if (!address) return '';

    const suffixPattern = /(镇|乡|街道|苏木|民族乡|地区|办事处)/;

    if (countyName) {
        const idx = address.indexOf(countyName);
        if (idx >= 0) {
            const afterCounty = address.slice(idx + countyName.length);
            const m = afterCounty.match(/^(.{1,20}?(?:镇|乡|街道|苏木|民族乡|地区|办事处))/);
            if (m?.[1] && suffixPattern.test(m[1])) return m[1];
        }
    }

    const m2 = address.match(/(.{1,20}?(?:镇|乡|街道|苏木|民族乡|地区|办事处))/);
    if (m2?.[1] && suffixPattern.test(m2[1])) return m2[1];

    return '';
}

function exportSichuanTxt() {
    console.log('Reading shp.geojson...');
    const shp = JSON.parse(fs.readFileSync(INPUT_FILE, 'utf8'));
    const countiesByCity = extractCityCountyTreeFromShp(shp);
    console.log(`Loaded ${Array.isArray(shp?.features) ? shp.features.length : 0} features.`);

    console.log('Reading village_points.geojson...');
    const points = JSON.parse(fs.readFileSync(VILLAGE_POINTS_FILE, 'utf8'));
    const { cityCodeByName, countyCodeByCityCounty, countyCodeByName } = extractCodesFromVillagePoints(points);

    const countyNameAliases = new Map([
        ['马尔康县', '马尔康市'],
        ['郫县', '郫都区'],
        ['双流县', '双流区'],
        ['新津县', '新津区'],
        ['罗江县', '罗江区'],
        ['康定县', '康定市'],
        ['彭山县', '彭山区'],
        ['安县', '安州区'],
        ['隆昌县', '隆昌市'],
        ['射洪县', '射洪市'],
        ['宜宾县', '叙州区']
    ]);

    const missingCities = [];
    const missingCounties = [];

    const cities = [...countiesByCity.keys()].sort((a, b) => a.localeCompare(b, 'zh-Hans-CN'));

    const lines = [];
    lines.push(`四川省\t510000`);
    for (const city of cities) {
        const cityCode = cityCodeByName.get(city) || '';
        if (!cityCode) missingCities.push(city);
        lines.push(`  ${city}\t${cityCode}`);

        const counties = [...(countiesByCity.get(city) || new Set())].sort((a, b) => a.localeCompare(b, 'zh-Hans-CN'));
        for (const county of counties) {
            const alias = countyNameAliases.get(county);
            let countyCode = countyCodeByCityCounty.get(`${city}||${county}`) || '';
            if (!countyCode && alias) {
                countyCode = countyCodeByCityCounty.get(`${city}||${alias}`) || '';
            }
            if (!countyCode) {
                countyCode = countyCodeByName.get(county) || '';
            }
            if (!countyCode && alias) {
                countyCode = countyCodeByName.get(alias) || '';
            }
            if (!countyCode) missingCounties.push(`${city}-${county}`);
            lines.push(`    ${county}\t${countyCode}`);
        }
    }

    fs.writeFileSync(OUTPUT_TXT_FILE, `${lines.join('\n')}\n`, 'utf8');
    console.log(`Saved: ${OUTPUT_TXT_FILE}`);

    if (missingCities.length || missingCounties.length) {
        console.warn(`Missing city codes: ${missingCities.length}`);
        console.warn(`Missing county codes: ${missingCounties.length}`);
        process.exitCode = 2;
    }
}

function exportCountyTownshipCommunityRelationsTxt() {
    console.log('Reading village_points.geojson...');
    const points = JSON.parse(fs.readFileSync(VILLAGE_POINTS_FILE, 'utf8'));
    const pointFeatures = Array.isArray(points?.features) ? points.features : [];

    const communityRowSet = new Set();
    const communityRows = [];
    for (const feature of pointFeatures) {
        const props = feature?.properties;
        if (!props) continue;
        const code = String(props.code || '').trim();
        if (code.length < 9) continue;
        const name = String(props.dzcun || '').trim();
        const countyName = String(props.dzxian || '').trim();
        if (!name || !countyName) continue;

        const townshipCode = code.slice(0, 9);
        const countyCode = code.slice(0, 6);
        const key = `${name}\t${townshipCode}\t${countyName}\t${countyCode}`;
        if (communityRowSet.has(key)) continue;
        communityRowSet.add(key);
        communityRows.push({ name, townshipCode, countyName, countyCode });
    }

    communityRows.sort((a, b) => {
        const byCounty = String(a.countyCode || a.countyName).localeCompare(String(b.countyCode || b.countyName), 'zh-Hans-CN');
        if (byCounty !== 0) return byCounty;
        const byTownship = String(a.townshipCode).localeCompare(String(b.townshipCode), 'zh-Hans-CN');
        if (byTownship !== 0) return byTownship;
        return String(a.name).localeCompare(String(b.name), 'zh-Hans-CN');
    });

    const lines = [];
    lines.push('社区/乡镇名称\t乡镇代码\t所属区县名称\t所属区县代码');
    for (const row of communityRows) {
        lines.push(`${row.name}\t${row.townshipCode}\t${row.countyName}\t${row.countyCode}`);
    }

    fs.writeFileSync(OUTPUT_COUNTY_TOWNSHIP_COMMUNITY_RELATION_FILE, `${lines.join('\n')}\n`, 'utf8');
    console.log(`Saved: ${OUTPUT_COUNTY_TOWNSHIP_COMMUNITY_RELATION_FILE}`);
}

function generateOrganizationBaselineSqlFromSichuanTxt(txtFilePath) {
    const raw = fs.readFileSync(txtFilePath, 'utf8');
    const lines = raw.split(/\r?\n/).filter(line => line.trim().length > 0);

    let provinceName = '';
    let provinceCode = '';
    let currentCityName = '';
    let currentCityCode = '';

    const rows = [];
    const escSql = (value) => String(value ?? '').replace(/\\/g, '\\\\').replace(/'/g, "''");

    for (const line of lines) {
        const indent = (line.match(/^\s*/) || [''])[0].length;
        const [nameRaw, codeRaw] = line.trim().split(/\t+/);
        const name = String(nameRaw || '').trim();
        const code = String(codeRaw || '').trim();
        if (!name || !code) continue;

        if (indent === 0) {
            provinceName = name;
            provinceCode = code.slice(0, 2);
            rows.push({
                code: provinceCode,
                name,
                level: 1,
                parentCode: null,
                provinceName,
                cityName: null,
                countyName: null
            });
            continue;
        }

        if (indent === 2) {
            currentCityName = name;
            currentCityCode = code.slice(0, 4);
            rows.push({
                code: currentCityCode,
                name,
                level: 2,
                parentCode: provinceCode,
                provinceName,
                cityName: currentCityName,
                countyName: null
            });
            continue;
        }

        const countyCode = code.slice(0, 6);
        rows.push({
            code: countyCode,
            name,
            level: 3,
            parentCode: currentCityCode,
            provinceName,
            cityName: currentCityName,
            countyName: name
        });
    }

    const baselineYear = 2020;
    const dataSource = 'TOWNSHIP';

    let sql = '';
    sql += 'SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;\n';
    sql += "SET collation_connection = 'utf8mb4_unicode_ci';\n";
    sql += 'START TRANSACTION;\n';
    sql += 'DROP TEMPORARY TABLE IF EXISTS tmp_sichuan_org;\n';
    sql += 'CREATE TEMPORARY TABLE tmp_sichuan_org (\n';
    sql += '  code varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,\n';
    sql += '  name varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,\n';
    sql += '  level tinyint NOT NULL,\n';
    sql += '  parent_code varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,\n';
    sql += '  province_name varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,\n';
    sql += '  city_name varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,\n';
    sql += '  county_name varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,\n';
    sql += '  PRIMARY KEY(code)\n';
    sql += ');\n';

    const chunkSize = 200;
    for (let i = 0; i < rows.length; i += chunkSize) {
        const chunk = rows.slice(i, i + chunkSize);
        sql += 'INSERT INTO tmp_sichuan_org (code,name,level,parent_code,province_name,city_name,county_name) VALUES\n';
        sql += chunk.map(r => {
            const parentCodeSql = r.parentCode ? `'${escSql(r.parentCode)}'` : 'NULL';
            const cityNameSql = r.cityName ? `'${escSql(r.cityName)}'` : 'NULL';
            const countyNameSql = r.countyName ? `'${escSql(r.countyName)}'` : 'NULL';
            return `('${escSql(r.code)}','${escSql(r.name)}',${r.level},${parentCodeSql},'${escSql(r.provinceName)}',${cityNameSql},${countyNameSql})`;
        }).join(',\n');
        sql += ';\n';
    }

    sql += 'INSERT INTO organization (parent_id, code, name, level, year, data_source, province_name, city_name, county_name, township_name, community_name, is_deleted, is_baseline, baseline_code)\n';
    sql += `SELECT NULL, t.code, t.name, t.level, ${baselineYear}, '${dataSource}', t.province_name, t.city_name, t.county_name, NULL, NULL, 0, 1, t.code\n`;
    sql += 'FROM tmp_sichuan_org t\n';
    sql += 'WHERE NOT EXISTS (\n';
    sql += '  SELECT 1 FROM organization o\n';
    sql += '  WHERE o.code = t.code AND o.is_baseline = 1 AND o.is_deleted = 0\n';
    sql += ');\n';

    sql += 'UPDATE organization o\n';
    sql += 'JOIN tmp_sichuan_org t ON o.code = t.code\n';
    sql += `SET o.name = t.name,\n`;
    sql += `    o.level = t.level,\n`;
    sql += `    o.year = ${baselineYear},\n`;
    sql += `    o.data_source = '${dataSource}',\n`;
    sql += `    o.province_name = t.province_name,\n`;
    sql += `    o.city_name = t.city_name,\n`;
    sql += `    o.county_name = t.county_name,\n`;
    sql += `    o.township_name = NULL,\n`;
    sql += `    o.community_name = NULL,\n`;
    sql += `    o.is_deleted = 0,\n`;
    sql += `    o.is_baseline = 1,\n`;
    sql += `    o.baseline_code = t.code\n`;
    sql += 'WHERE o.is_baseline = 1 AND o.is_deleted = 0;\n';

    sql += 'UPDATE organization o\n';
    sql += 'JOIN tmp_sichuan_org t ON o.code = t.code\n';
    sql += 'LEFT JOIN organization p ON p.code = t.parent_code AND p.is_baseline = 1 AND p.is_deleted = 0\n';
    sql += 'SET o.parent_id = p.id\n';
    sql += 'WHERE o.is_baseline = 1 AND o.is_deleted = 0;\n';

    sql += 'COMMIT;\n';
    return sql;
}

function applySqlToMysql(sql) {
    const host = process.env.MYSQL_HOST || '127.0.0.1';
    const port = process.env.MYSQL_PORT || '30314';
    const user = process.env.MYSQL_USER || 'root';
    const password = process.env.MYSQL_PASSWORD || '123456';
    const database = process.env.MYSQL_DATABASE || 'evaluate_db';

    const args = ['-h', host, '-P', String(port), '-u', user, `-p${password}`, database];
    const result = spawnSync('mysql', args, { input: sql, encoding: 'utf8' });
    if (result.stdout) process.stdout.write(result.stdout);
    if (result.stderr) process.stderr.write(result.stderr);
    if (result.status !== 0) {
        process.exitCode = result.status || 1;
    }
}

function applyOrganizationBaselineFromSichuanTxt() {
    const sql = generateOrganizationBaselineSqlFromSichuanTxt(SICHUAN_ORG_TXT_FILE);
    applySqlToMysql(sql);
}

function buildTownshipAndCommunityIndexesFromVillagePoints(pointsGeojson) {
    const townshipNameByCode = new Map();
    const communityCodeByTownshipAndName = new Map();

    const features = Array.isArray(pointsGeojson?.features) ? pointsGeojson.features : [];
    for (const feature of features) {
        const props = feature?.properties;
        if (!props) continue;

        const code = String(props.code || '').trim();
        if (code.length < 9) continue;

        const townshipCode = code.slice(0, 9);
        const countyName = String(props.dzxian || '').trim();
        const address = String(props.address || '').trim();
        const townshipName = extractTownshipNameFromAddress(address, countyName);
        if (townshipName && !townshipNameByCode.has(townshipCode)) {
            townshipNameByCode.set(townshipCode, townshipName);
        }

        const communityName = String(props.dzcun || '').trim();
        if (communityName && code.length >= 12) {
            const key = `${townshipCode}||${communityName}`;
            if (!communityCodeByTownshipAndName.has(key)) {
                communityCodeByTownshipAndName.set(key, code.slice(0, 12));
            }
        }
    }

    return { townshipNameByCode, communityCodeByTownshipAndName };
}

function generateGrassrootsBaselineSqlFromSichuanRelationTxt(txtFilePath) {
    const raw = fs.readFileSync(txtFilePath, 'utf8');
    const lines = raw.split(/\r?\n/).filter((line) => line.trim().length > 0);

    if (lines.length <= 1) {
        throw new Error(`No data rows in: ${txtFilePath}`);
    }

    const points = JSON.parse(fs.readFileSync(VILLAGE_POINTS_FILE, 'utf8'));
    const { townshipNameByCode, communityCodeByTownshipAndName } = buildTownshipAndCommunityIndexesFromVillagePoints(points);

    const escSql = (value) => String(value ?? '').replace(/\\/g, '\\\\').replace(/'/g, "''");
    const hashSuffix = (input, len = 6) => crypto.createHash('md5').update(String(input)).digest('hex').slice(0, len);

    const baselineYear = 2020;
    const dataSource = 'IMPORT';

    const townshipRowsByCode = new Map();
    const communityRows = [];

    for (let i = 1; i < lines.length; i++) {
        const line = lines[i];
        const parts = line.split('\t');
        if (parts.length < 4) continue;
        const communityName = String(parts[0] || '').trim();
        const townshipCode = String(parts[1] || '').trim();
        const countyCode = String(parts[3] || '').trim();

        if (!communityName || townshipCode.length < 9 || countyCode.length < 6) continue;

        const normalizedTownshipCode = townshipCode.slice(0, 9);
        const normalizedCountyCode = countyCode.slice(0, 6);
        const townshipName = townshipNameByCode.get(normalizedTownshipCode) || normalizedTownshipCode;

        if (!townshipRowsByCode.has(normalizedTownshipCode)) {
            townshipRowsByCode.set(normalizedTownshipCode, {
                countyCode: normalizedCountyCode,
                townshipCode: normalizedTownshipCode,
                townshipName
            });
        }

        const communityKey = `${normalizedTownshipCode}||${communityName}`;
        const resolvedCommunityCode = communityCodeByTownshipAndName.get(communityKey);
        const communityCode = resolvedCommunityCode || `${normalizedTownshipCode}${hashSuffix(`${normalizedCountyCode}||${communityName}`, 8)}`;

        communityRows.push({
            countyCode: normalizedCountyCode,
            townshipCode: normalizedTownshipCode,
            townshipName,
            communityCode,
            communityName
        });
    }

    const townshipRows = [...townshipRowsByCode.values()];

    let sql = '';
    sql += 'SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;\n';
    sql += "SET collation_connection = 'utf8mb4_unicode_ci';\n";
    sql += 'START TRANSACTION;\n';

    sql += 'DROP TEMPORARY TABLE IF EXISTS tmp_sichuan_grassroots;\n';
    sql += 'CREATE TEMPORARY TABLE tmp_sichuan_grassroots (\n';
    sql += '  county_code varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,\n';
    sql += '  township_code varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,\n';
    sql += '  township_name varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,\n';
    sql += '  community_code varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,\n';
    sql += '  community_name varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,\n';
    sql += '  PRIMARY KEY(community_code)\n';
    sql += ');\n';

    const chunkSize = 200;
    for (let i = 0; i < communityRows.length; i += chunkSize) {
        const chunk = communityRows.slice(i, i + chunkSize);
        sql += 'INSERT INTO tmp_sichuan_grassroots (county_code,township_code,township_name,community_code,community_name) VALUES\n';
        sql += chunk.map((r) => {
            return `('${escSql(r.countyCode)}','${escSql(r.townshipCode)}','${escSql(r.townshipName)}','${escSql(r.communityCode)}','${escSql(r.communityName)}')`;
        }).join(',\n');
        sql += ';\n';
    }

    sql += 'DROP TEMPORARY TABLE IF EXISTS tmp_sichuan_townships;\n';
    sql += 'CREATE TEMPORARY TABLE tmp_sichuan_townships (\n';
    sql += '  county_code varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,\n';
    sql += '  township_code varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,\n';
    sql += '  township_name varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,\n';
    sql += '  PRIMARY KEY(township_code)\n';
    sql += ');\n';

    for (let i = 0; i < townshipRows.length; i += chunkSize) {
        const chunk = townshipRows.slice(i, i + chunkSize);
        sql += 'INSERT INTO tmp_sichuan_townships (county_code,township_code,township_name) VALUES\n';
        sql += chunk.map((r) => {
            return `('${escSql(r.countyCode)}','${escSql(r.townshipCode)}','${escSql(r.townshipName)}')`;
        }).join(',\n');
        sql += ';\n';
    }

    sql += 'INSERT INTO grassroots_organization (county_id, parent_id, code, name, level, year, data_source, province_name, city_name, county_name, township_name, community_name, is_deleted, is_baseline, baseline_code)\n';
    sql += `SELECT o.id, NULL, t.township_code, t.township_name, 4, ${baselineYear}, '${dataSource}', o.province_name, o.city_name, o.name, t.township_name, NULL, 0, 1, t.township_code\n`;
    sql += 'FROM tmp_sichuan_townships t\n';
    sql += 'JOIN organization o ON o.code = t.county_code AND o.is_baseline = 1 AND o.is_deleted = 0\n';
    sql += 'WHERE NOT EXISTS (\n';
    sql += '  SELECT 1 FROM grassroots_organization g\n';
    sql += `  WHERE g.code = t.township_code AND g.year = ${baselineYear} AND g.is_baseline = 1 AND g.is_deleted = 0\n`;
    sql += ');\n';

    sql += 'UPDATE grassroots_organization g\n';
    sql += 'JOIN tmp_sichuan_townships t ON g.code = t.township_code\n';
    sql += 'JOIN organization o ON o.code = t.county_code AND o.is_baseline = 1 AND o.is_deleted = 0\n';
    sql += `SET g.county_id = o.id,\n`;
    sql += `    g.parent_id = NULL,\n`;
    sql += `    g.name = t.township_name,\n`;
    sql += `    g.level = 4,\n`;
    sql += `    g.year = ${baselineYear},\n`;
    sql += `    g.data_source = '${dataSource}',\n`;
    sql += `    g.province_name = o.province_name,\n`;
    sql += `    g.city_name = o.city_name,\n`;
    sql += `    g.county_name = o.name,\n`;
    sql += `    g.township_name = t.township_name,\n`;
    sql += `    g.community_name = NULL,\n`;
    sql += `    g.is_deleted = 0,\n`;
    sql += `    g.is_baseline = 1,\n`;
    sql += `    g.baseline_code = g.code\n`;
    sql += `WHERE g.year = ${baselineYear} AND g.is_baseline = 1 AND g.is_deleted = 0;\n`;

    sql += 'INSERT INTO grassroots_organization (county_id, parent_id, code, name, level, year, data_source, province_name, city_name, county_name, township_name, community_name, is_deleted, is_baseline, baseline_code)\n';
    sql += `SELECT o.id, gt.id, r.community_code, r.community_name, 5, ${baselineYear}, '${dataSource}', o.province_name, o.city_name, o.name, r.township_name, r.community_name, 0, 1, r.community_code\n`;
    sql += 'FROM tmp_sichuan_grassroots r\n';
    sql += 'JOIN organization o ON o.code = r.county_code AND o.is_baseline = 1 AND o.is_deleted = 0\n';
    sql += `JOIN grassroots_organization gt ON gt.code = r.township_code AND gt.level = 4 AND gt.year = ${baselineYear} AND gt.is_baseline = 1 AND gt.is_deleted = 0\n`;
    sql += 'WHERE NOT EXISTS (\n';
    sql += '  SELECT 1 FROM grassroots_organization g\n';
    sql += `  WHERE g.code = r.community_code AND g.year = ${baselineYear} AND g.is_baseline = 1 AND g.is_deleted = 0\n`;
    sql += ');\n';

    sql += 'UPDATE grassroots_organization g\n';
    sql += 'JOIN tmp_sichuan_grassroots r ON g.code = r.community_code\n';
    sql += 'JOIN organization o ON o.code = r.county_code AND o.is_baseline = 1 AND o.is_deleted = 0\n';
    sql += `JOIN grassroots_organization gt ON gt.code = r.township_code AND gt.level = 4 AND gt.year = ${baselineYear} AND gt.is_baseline = 1 AND gt.is_deleted = 0\n`;
    sql += `SET g.county_id = o.id,\n`;
    sql += `    g.parent_id = gt.id,\n`;
    sql += `    g.name = r.community_name,\n`;
    sql += `    g.level = 5,\n`;
    sql += `    g.year = ${baselineYear},\n`;
    sql += `    g.data_source = '${dataSource}',\n`;
    sql += `    g.province_name = o.province_name,\n`;
    sql += `    g.city_name = o.city_name,\n`;
    sql += `    g.county_name = o.name,\n`;
    sql += `    g.township_name = r.township_name,\n`;
    sql += `    g.community_name = r.community_name,\n`;
    sql += `    g.is_deleted = 0,\n`;
    sql += `    g.is_baseline = 1,\n`;
    sql += `    g.baseline_code = g.code\n`;
    sql += `WHERE g.year = ${baselineYear} AND g.is_baseline = 1 AND g.is_deleted = 0;\n`;

    sql += 'COMMIT;\n';
    return sql;
}

function applyGrassrootsBaselineFromSichuanRelationTxt() {
    const sql = generateGrassrootsBaselineSqlFromSichuanRelationTxt(SICHUAN_COUNTY_TOWNSHIP_COMMUNITY_RELATION_TXT_FILE);
    applySqlToMysql(sql);
}

async function generateRegionHierarchyJson() {
    const turf = await import('@turf/turf');

    console.log('Reading shp.geojson...');
    const rawData = fs.readFileSync(INPUT_FILE, 'utf8');
    const geojson = JSON.parse(rawData);

    console.log(`Loaded ${geojson.features.length} features.`);

    const countyFeatures = {};
    const cityFeatures = {};
    const provinceFeatures = [];

    function addToGroup(group, key, feature) {
        if (!group[key]) {
            group[key] = [];
        }
        group[key].push(feature);
    }

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

        let currentTolerance = tolerance;
        let simplified = turf.simplify(merged, { tolerance: currentTolerance, highQuality: true });

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

        let coordCount = countCoords(simplified.geometry);

        let attempts = 0;
        while (coordCount > 200 && attempts < 10) {
            currentTolerance *= 1.5;
            simplified = turf.simplify(merged, { tolerance: currentTolerance, highQuality: false });
            coordCount = countCoords(simplified.geometry);
            attempts++;
        }

        console.log(`  ${name} simplified to ${coordCount} points (tol: ${currentTolerance.toFixed(5)})`);

        simplified.properties = {
            name: name,
            level: level,
            childCount: features.length
        };

        return simplified;
    }

    const treeRoot = {
        name: "四川省",
        level: "province",
        code: "510000",
        children: [],
        geometry: null,
        bbox: null,
        center: null
    };

    const cityNodes = {};

    console.log('Processing Province...');
    const provFeature = processFeatures(provinceFeatures, '四川省', 'province', 0.005);
    if (provFeature) {
        const bbox = turf.bbox(provFeature);
        const center = turf.centerOfMass(provFeature).geometry.coordinates;
        treeRoot.geometry = provFeature.geometry;
        treeRoot.bbox = bbox;
        treeRoot.center = center;
    }

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
                children: [],
                geometry: cityFeat.geometry,
                bbox: bbox,
                center: center
            };

            treeRoot.children.push(cityNode);
            cityNodes[cityName] = cityNode;
        }
    });

    const countyKeys = Object.keys(countyFeatures);
    console.log(`Processing ${countyKeys.length} Counties...`);
    countyKeys.forEach(countyName => {
        const feats = countyFeatures[countyName];
        const sampleProps = feats[0].properties;
        const parentCityName = (sampleProps.CITY || sampleProps.city || '').trim();

        const countyFeat = processFeatures(feats, countyName, 'county', 0.001);

        if (countyFeat) {
            const bbox = turf.bbox(countyFeat);
            const center = turf.centerOfMass(countyFeat).geometry.coordinates;

            const countyNode = {
                name: countyName,
                level: 'county',
                geometry: countyFeat.geometry,
                bbox: bbox,
                center: center
            };

            if (parentCityName && cityNodes[parentCityName]) {
                cityNodes[parentCityName].children.push(countyNode);
            } else {
                console.warn(`  Orphan county: ${countyName} (City: ${parentCityName})`);
            }
        }
    });

    const outputData = {
        "四川省": treeRoot
    };

    console.log('Saving hierarchy...');
    fs.writeFileSync(OUTPUT_FILE, JSON.stringify(outputData, null, 2));
}

const args = process.argv.slice(2);
if (args.includes('--export-sichuan-txt') || args.includes('--export-txt-only')) {
    exportSichuanTxt();
} else if (args.includes('--export-county-township-community-relations')) {
    exportCountyTownshipCommunityRelationsTxt();
} else if (args.includes('--apply-org-baseline-from-sichuan-txt')) {
    applyOrganizationBaselineFromSichuanTxt();
} else if (args.includes('--apply-grassroots-baseline-from-sichuan-relation-txt')) {
    applyGrassrootsBaselineFromSichuanRelationTxt();
} else {
    generateRegionHierarchyJson().then(() => {
        console.log('Done.');
    }).catch((e) => {
        console.error(e);
        process.exitCode = 1;
    });
}
