-- 查询所有乡镇的医疗保障能力原始数据
SELECT 
    township AS '乡镇名称',
    hospital_beds AS '住院床位数',
    population AS '常住人口',
    ROUND((hospital_beds / population) * 10000, 8) AS '医疗保障能力原始值'
FROM survey_data
WHERE hospital_beds IS NOT NULL 
  AND population IS NOT NULL 
  AND population > 0
ORDER BY township;

-- 计算归一化所需的平方和
SELECT 
    ROUND(SQRT(SUM(POW((hospital_beds / population) * 10000, 2))), 8) AS '平方根',
    COUNT(*) AS '乡镇数量'
FROM survey_data
WHERE hospital_beds IS NOT NULL 
  AND population IS NOT NULL 
  AND population > 0;

-- 显示每个乡镇的归一化值
SELECT 
    township AS '乡镇名称',
    ROUND((hospital_beds / population) * 10000, 8) AS '原始值',
    ROUND(
        ((hospital_beds / population) * 10000) / 
        (SELECT SQRT(SUM(POW((hospital_beds / population) * 10000, 2)))
         FROM survey_data
         WHERE hospital_beds IS NOT NULL AND population IS NOT NULL AND population > 0),
        8
    ) AS '归一化值'
FROM survey_data
WHERE hospital_beds IS NOT NULL 
  AND population IS NOT NULL 
  AND population > 0
ORDER BY township;
