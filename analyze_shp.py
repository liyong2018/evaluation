import json
import os
import sys

input_file = r'd:\Evaluation\evaluation\frontend\public\shp.geojson'

try:
    with open(input_file, 'r', encoding='utf-8') as f:
        data = json.load(f)

    city_counts = {}
    
    for feature in data['features']:
        props = feature.get('properties', {})
        city = props.get('CITY', 'Unknown')
        if not city:
            city = 'Unknown'
        
        city_counts[city] = city_counts.get(city, 0) + 1

    print(f"Total features: {len(data['features'])}")
    print("Features per city:")
    for city, count in sorted(city_counts.items(), key=lambda x: x[1], reverse=True):
        print(f"{city}: {count}")

except Exception as e:
    print(f"Error: {e}")
