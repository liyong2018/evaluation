# SQL 迁移脚本索引（sql/migrations）

此清单汇总 sql/migrations/ 下的重要迁移脚本，按文件名与用途简要说明，并给出推荐执行顺序。

推荐顺序（如目标库尚未建表或需初始化）
- create_region_data_table.sql（如尚未创建地区数据表，先执行）
- 001_modify_formula_config_algorithm_step_id.sql
- 002_create_model_management.sql
- 003_init_model_formulas.sql
- 004_fix_chinese_encoding.sql（按需）

脚本说明
- create_region_data_table.sql
  - 功能：创建地区数据相关表结构（region 数据初始化或结构准备）。
  - 说明：仅当目标库缺少相关表时执行；已存在可跳过。

- 001_modify_formula_config_algorithm_step_id.sql
  - 功能：调整/补充 formula_config 与算法步骤的关联字段（如 algorithm_step_id），保证与步骤配置一致。
  - 依赖：相关表已存在；可在结构初始化后执行。

- 002_create_model_management.sql
  - 功能：创建/更新模型管理相关表（如 model_step、step_algorithm、algorithm_config 等）。
  - 依赖：无硬性依赖；建议在 001 之后，以保持结构一致性。

- 003_init_model_formulas.sql
  - 功能：初始化/插入模型公式与规则配置，便于按步骤执行算法。
  - 依赖：需先完成模型管理表结构创建（见 002）。

- 004_fix_chinese_encoding.sql
  - 功能：修复中文编码或批量内容编码问题（如 UTF-8/排序规则），统一字符集。
  - 说明：按需执行；建议在数据导入完成后进行。

维护说明
- 新增迁移脚本时，请在“推荐顺序”与“脚本说明”补充条目，标注依赖与是否可跳过。
- 诊断/校验类 SQL 请放在 sql/diagnostics/，零散历史脚本放在 sql/archive/。
