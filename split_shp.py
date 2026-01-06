import json
import os
import sys

input_file = r'd:\Evaluation\evaluation\frontend\public\shp.geojson'
output_dir = r'd:\Evaluation\evaluation\frontend\public\boundaries\city'

if not os.path.exists(output_dir):
    os.makedirs(output_dir)

try:
    print(f"Reading {input_file}...")
    with open(input_file, 'r', encoding='utf-8') as f:
        data = json.load(f)

    city_features = {}
    
    print("Grouping features by city...")
    for feature in data['features']:
        props = feature.get('properties', {})
        city = props.get('CITY', 'Unknown')
        if not city:
            city = 'Unknown'
        
        if city not in city_features:
            city_features[city] = []
        city_features[city].append(feature)

    print(f"Splitting into {len(city_features)} files...")
    for city, features in city_features.items():
        if not city or city == 'Unknown':
            continue
            
        output_path = os.path.join(output_dir, f"{city}.json")
        city_data = {
            "type": "FeatureCollection",
            "name": city,
            "features": features
        }
        
        with open(output_path, 'w', encoding='utf-8') as f:
            json.dump(city_data, f, ensure_ascii=False)
        
        print(f"Saved {city}: {len(features)} features")

    print("Done.")

except Exception as e:
    print(f"Error: {e}")
