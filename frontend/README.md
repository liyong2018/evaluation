# 前端说明

## 项目简介

本前端项目是减灾能力评估系统的用户界面，采用 Vue 3、Vite 和 Element Plus 构建。

## 最新进展

- **2025-09-24**: 与后端 API 进行了联调测试，修复了数据显示和交互方面的一些问题，提升了系统的稳定性和用户体验。

This template should help get you started developing with Vue 3 in Vite.

## Recommended IDE Setup

[VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Type Support for `.vue` Imports in TS

TypeScript cannot handle type information for `.vue` imports by default, so we replace the `tsc` CLI with `vue-tsc` for type checking. In editors, we need [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) to make the TypeScript language service aware of `.vue` types.

## Customize configuration

See [Vite Configuration Reference](https://vite.dev/config/).

## Project Setup

```sh
npm install
```

### Compile and Hot-Reload for Development

```sh
npm run dev
```

### Type-Check, Compile and Minify for Production

```sh
npm run build
```

### Lint with [ESLint](https://eslint.org/)

```sh
npm run lint
```

## Project Status

- **2024-07-22**: Fixed a bug in the Word export feature where `getCapabilityLevelText` was not defined.
- **2024-07-22**: Fixed a bug in the thematic map where boundary data was not being filtered correctly by county name.
