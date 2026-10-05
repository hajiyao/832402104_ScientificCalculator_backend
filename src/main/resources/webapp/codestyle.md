# 前端代码规范（codestyle.md）

**标准来源：[Airbnb JavaScript Style Guide](https://github.com/airbnb/javascript)、
[Google HTML/CSS Style Guide](https://google.github.io/styleguide/htmlcssguide.html)。**
本项目在上述标准基础上结合作业实际情况做了少量约定，具体如下。

## 1. 通用

- 源文件统一使用 UTF-8 编码，缩进使用 4 个空格，不使用 Tab。
- 文件末尾保留一个空行，每行不超过 100 个字符。
- 所有语句末尾加分号。
- 不使用前端框架，全部为原生 HTML / CSS / JavaScript。

## 2. HTML

- 标签、属性名全部小写，属性值使用双引号。
- 正确声明 `<!DOCTYPE html>`、`lang`、`charset` 和 `viewport`。
- 结构语义化，标题用 `h1~h3`，列表用 `ul/li`，区块用 `section/header/main`。
- 不在 HTML 中写内联样式和内联事件（`onclick=""`），样式与行为全部放到独立文件。

## 3. CSS

- 类名使用小写字母加连字符，例如 `history-item`、`screen-result`。
- 每个选择器的属性集中书写，顺序为：布局定位 → 盒模型 → 文字颜色 → 其他。
- 颜色统一使用十六进制或 `rgba()`，不混用多种写法。
- 不滥用 `!important`。
- 响应式通过媒体查询实现，断点统一为 760px。

## 4. JavaScript

- 变量统一使用 `var`（本项目按 ES5 编写），命名使用小驼峰。
- 常量使用全大写加下划线，例如 `OPERATOR_TOKENS`。
- 字符串使用单引号，需要拼接 HTML 属性等场景除外。
- 使用严格模式（`'use strict'`），代码放在 IIFE 中避免污染全局。
- 比较使用 `===` 和 `!==`，不使用 `==`。
- DOM 查询结果缓存到变量，不重复查询。
- 动态内容一律使用 `textContent` / `createElement` 渲染，禁止用 `innerHTML`
  拼接用户或后端返回的内容，防止注入。
- 接口请求统一封装，错误信息统一展示，不在各处重复写 fetch。

## 5. 职责边界

- 前端只负责交互和展示，**禁止在前端实现表达式计算**，不允许出现 `eval`。
- 不在 `localStorage`、内存中保存历史记录数据，历史一律从后端接口获取。
- 后端地址只在 `js/config.js` 中配置一处。
