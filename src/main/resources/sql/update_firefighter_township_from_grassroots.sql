-- 更新 firefighter_config 表的 township_name 字段
-- 从 grassroots_organization 表中获取乡镇名称
-- 通过 region_code 的前9位（乡镇级代码）匹配 grassroots_organization 的 code

UPDATE firefighter_config f
INNER JOIN grassroots_organization g
  ON SUBSTRING(f.region_code, 1, 9) = g.code
SET f.township_name = g.township_name,
    f.updated_time = NOW()
WHERE g.level = 4
  AND (f.township_name = '' OR f.township_name IS NULL);

-- 查看更新结果
SELECT
    f.region_code,
    f.province_name,
    f.city_name,
    f.county_name,
    f.township_name,
    g.township_name AS expected_township,
    f.firefighter_count
FROM firefighter_config f
LEFT JOIN grassroots_organization g
  ON SUBSTRING(f.region_code, 1, 9) = g.code
WHERE g.level = 2
ORDER BY f.region_code;
