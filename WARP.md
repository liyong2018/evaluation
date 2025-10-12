# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

This is a **Disaster Reduction Capability Evaluation System (减灾能力评估系统)** - a full-stack application for assessing regional disaster reduction capabilities using multi-criteria decision analysis algorithms. The system processes survey data from townships and counties to evaluate their preparedness across various disaster management dimensions.

### Technology Stack
- **Backend**: Spring Boot 2.7.18 (Java 8), MyBatis Plus, MySQL
- **Frontend**: Vue 3 + TypeScript, Vite, Element Plus, Leaflet (maps), ECharts (charts)
- **Database**: MySQL 8.0 with comprehensive data model for evaluations
- **Architecture**: REST API backend with responsive Vue.js frontend

## Common Development Commands

### Backend (Spring Boot)
```powershell
# Build and run backend
mvn clean install
mvn spring-boot:run

# Run tests
mvn test

# Package for deployment
mvn clean package
```

### Frontend (Vue 3 + Vite)
```powershell
cd frontend

# Install dependencies
npm install

# Development server
npm run dev

# Build for production
npm run build

# Type checking
npm run type-check

# Lint and fix
npm run lint
```

### Database Operations
```powershell
# Initialize database (run once)
mysql -u root -p < init_database.sql

# Apply migrations
mysql -u root -p < migrations/001_modify_formula_config_algorithm_step_id.sql
```

### Development Workflow
```powershell
# Start both servers for development
# Terminal 1: Backend
mvn spring-boot:run

# Terminal 2: Frontend
cd frontend && npm run dev
```

## High-Level Architecture

### Core Domain Model
The system implements a **multi-step evaluation algorithm** with these key entities:

1. **Survey Data (`survey_data`)** - Raw township/county statistics (population, resources, capabilities)
2. **Weight Configuration (`weight_config`, `indicator_weight`)** - Hierarchical weighting system for evaluation criteria
3. **Algorithm Configuration (`algorithm_config`, `algorithm_step`, `formula_config`)** - Configurable evaluation algorithms and mathematical formulas
4. **Results (`secondary_indicator_result`, `primary_indicator_result`)** - Multi-level evaluation outcomes
5. **Reports (`report`)** - Generated assessment documents and thematic maps

### Evaluation Algorithm Flow
The system implements a **5-step TOPSIS-based evaluation algorithm**:

1. **Secondary Indicator Calculation** - Transforms raw survey data into 8 capability metrics
2. **Vector Normalization** - Normalizes indicators for comparison
3. **Weight Application** - Applies configurable weights to indicators  
4. **TOPSIS Algorithm** - Calculates distances to ideal/anti-ideal solutions
5. **Capability Grading** - Classifies results into 5 capability levels (强/较强/中等/较弱/弱)

### API Architecture
The backend follows a **layered REST API structure**:
- **Controllers**: Handle HTTP requests (`/api/*` endpoints)
- **Services**: Business logic for evaluation algorithms
- **Mappers**: MyBatis data access layer
- **Entities**: JPA/MyBatis domain models

Key API modules:
- `surveyDataApi` - CRUD for survey data with Excel import/export
- `weightConfigApi` - Manage evaluation criteria weights
- `algorithmManagementApi` - Configure evaluation algorithms and formulas
- `evaluationApi` - Execute multi-step evaluations
- `thematicMapApi` - Generate geographical visualizations

### Frontend Architecture
Vue 3 application with **composition API and TypeScript**:

- **Views**: Main application pages (`Dashboard`, `DataManagement`, `Evaluation`, etc.)
- **Components**: Reusable UI components (dialogs, charts, maps)
- **API Layer**: Centralized HTTP client with request utilities
- **Router**: SPA navigation between evaluation workflow steps

Key features:
- **Data Management**: Excel import/export with validation
- **Interactive Evaluation**: Step-by-step algorithm execution with real-time results
- **Thematic Mapping**: Leaflet-based choropleth maps showing evaluation results
- **Results Visualization**: ECharts integration for capability analysis

## Configuration Notes

### Database Configuration
- Default MySQL connection: `127.0.0.1:3306/evaluate_db`
- Credentials in `application.yml` (change for production)
- MyBatis Plus with auto-configuration for entities

### Frontend Development
- Vite dev server runs on `http://localhost:5173`
- Configured for LAN access (`host: '0.0.0.0'`)
- Proxy configuration may be needed for backend API calls in development

### Algorithm Customization
The evaluation algorithm is **fully configurable** through the database:
- Add new evaluation steps in `algorithm_step` table
- Define custom formulas in `formula_config` using expression syntax
- Modify indicator weights without code changes
- Support for multiple algorithm configurations

## Key Development Considerations

### Data Model Relationships
- Survey data links to results through foreign keys
- Weight configurations support hierarchical indicator structures (L1/L2 indicators)
- Algorithm steps reference multiple formulas (many-to-many via comma-separated IDs)
- Results maintain full calculation traceability

### Algorithm Extensibility
- Formula expressions support mathematical functions and conditional logic
- New evaluation algorithms can be added by defining steps and formulas
- TOPSIS implementation is generalized for different indicator sets
- Support for batch evaluation across multiple regions

### Map Integration
- Leaflet with China TMS providers for base maps
- Choropleth visualization of evaluation results
- Region boundary data integration for administrative divisions
- Export capabilities for generated thematic maps

This system is designed for **flexibility and scalability** in disaster management assessment, with configurable algorithms and comprehensive data tracking throughout the evaluation process.

## QLExpress Dynamic Rule Engine (NEW)

### Overview
The system now includes a **QLExpress-based dynamic rule engine** for algorithm configuration, allowing formulas to be modified without code changes.

### Key Features
- **Model Management**: Create and manage multiple evaluation models
- **Step Configuration**: Define evaluation workflow with ordered steps
- **Algorithm Expressions**: Configure QLExpress formulas for each step
- **Real-time Validation**: Syntax checking for QLExpress expressions
- **Execution Tracking**: Complete audit trail of model executions

### Database Structure
```
evaluation_model           # 评估模型
├── model_step             # 模型步骤 (1-7 steps)
│   └── step_algorithm     # 步骤算法 (QLExpress expressions)
└── model_execution_record # 执行记录
    └── step_execution_result # 步骤结果
```

### Common QLExpress Commands

```powershell
# Access model management API
Invoke-WebRequest -Uri "http://localhost:8081/api/model-management/models" -Method GET

# Validate QLExpress expression
Invoke-WebRequest -Uri "http://localhost:8081/api/model-management/validate-expression" `
    -Method POST `
    -Body '{"expression": "(management_staff / population) * 10000"}' `
    -ContentType "application/json"
```

### QLExpress Expression Examples

**Basic Calculations:**
```java
(management_staff / population) * 10000
risk_assessment == "是" ? 1 : 0
```

**Statistical Functions:**
```java
AVERAGE(values)  // 平均值
STDEV(values)    // 标准差
SUMSQ(values)    // 平方和
SQRT(value)      // 平方根
```

**TOPSIS Algorithm:**
```java
positive_distance = SQRT(SUMSQ(max_values - current_values))
negative_distance = SQRT(SUMSQ(min_values - current_values))
topsis_score = negative_distance / (negative_distance + positive_distance)
```

### Model Configuration Workflow

1. **Create Model** → 2. **Add Steps** → 3. **Configure Algorithms** → 4. **Execute Evaluation**

#### Step Types:
- `CALCULATION` - Indicator calculation from survey data
- `NORMALIZATION` - Vector normalization
- `WEIGHTING` - Apply weights to indicators
- `TOPSIS` - Calculate ideal/anti-ideal solution distances
- `GRADING` - Classify capability levels

### API Endpoints

```
GET    /api/model-management/models                    # List all models
GET    /api/model-management/models/{id}/detail        # Get model details
POST   /api/model-management/models                    # Create model
POST   /api/model-management/models/{id}/steps         # Add step
POST   /api/model-management/steps/{id}/algorithms     # Add algorithm
PUT    /api/model-management/algorithms/{id}           # Update algorithm
POST   /api/model-management/validate-expression       # Validate QLExpress
```

### Migration Files
- `migrations/002_create_model_management.sql` - Model tables structure
- `migrations/003_init_model_formulas.sql` - Default formulas initialization

### Related Documentation
- `QLExpress实施指南.md` - Detailed implementation guide
- `完成总结.md` - Implementation summary and next steps
