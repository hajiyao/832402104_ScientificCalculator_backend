# 计算器前端

计算器系统的 Web 前端。只负责界面和交互，不做计算：输入表达式，按等号，
把表达式发给后端，显示后端返回的结果和历史记录。

## 技术栈

HTML + CSS + 原生 JavaScript，没有框架，不需要构建。

## 目录

```
index.html
css/style.css
js/config.js    后端地址
js/app.js       交互和请求
```

## 运行

推荐由后端一起托管：把本目录的文件复制到后端的
`src/main/resources/webapp/`，启动后端，访问后端地址。

也可以单独打开 `index.html`（或起个静态服务），这种情况下要先在
`js/config.js` 里填后端地址：

```js
window.API_BASE = 'http://localhost:8080';
```

需要后端先启动。

## 功能

- 加减乘除、括号、小数、正负号
- 乘方、开方、阶乘、取模、三角函数、反三角函数、对数、指数、π、e、Ans
- DEG/RAD 切换
- 历史记录的查询、删除单条、清空
- 键盘输入，回车计算，退格删除，Esc 清空

## 接口

| 操作 | 请求 |
|---|---|
| 计算 | `POST /api/calculate` |
| 查历史 | `GET /api/history` |
| 删一条 | `DELETE /api/history/{id}` |
| 清空 | `DELETE /api/history` |
