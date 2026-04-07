<<<<<<< HEAD
# front

This template should help get you started developing with Vue 3 in Vite.

## Recommended IDE Setup

[VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur) + [TypeScript Vue Plugin (Volar)](https://marketplace.visualstudio.com/items?itemName=Vue.vscode-typescript-vue-plugin).

## Type Support for `.vue` Imports in TS

TypeScript cannot handle type information for `.vue` imports by default, so we replace the `tsc` CLI with `vue-tsc` for type checking. In editors, we need [TypeScript Vue Plugin (Volar)](https://marketplace.visualstudio.com/items?itemName=Vue.vscode-typescript-vue-plugin) to make the TypeScript language service aware of `.vue` types.

If the standalone TypeScript plugin doesn't feel fast enough to you, Volar has also implemented a [Take Over Mode](https://github.com/johnsoncodehk/volar/discussions/471#discussioncomment-1361669) that is more performant. You can enable it by the following steps:

1. Disable the built-in TypeScript Extension
    1) Run `Extensions: Show Built-in Extensions` from VSCode's command palette
    2) Find `TypeScript and JavaScript Language Features`, right click and select `Disable (Workspace)`
2. Reload the VSCode window by running `Developer: Reload Window` from the command palette.

## Customize configuration

See [Vite Configuration Reference](https://vitejs.dev/config/).

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
=======
## 一、系统技术架构

|     技术层级     |                         核心技术选型                         |               关键说明               |
| :--------------: | :----------------------------------------------------------: | :----------------------------------: |
|     前端核心     | Vue 3（Composition API + `<script setup>`）、TypeScript、Vite |     保证类型安全，开发热更新高效     |
| 前端 UI / 可视化 |     Element Plus、自定义 Canvas/SVG 国风组件、AntV G2/G6     | 基础 UI + 定制化时间轴 + 数据可视化  |
| 前端状态 / 路由  |                     Pinia、Vue Router 4                      |      轻量状态管理，支持动态路由      |
|   前端特色能力   | Canvas（时间轴）、Leaflet/Mapbox（地图）、Quill.js（富文本）、jsPDF+html2canvas（PDF 导出） |        核心交互与导出能力支撑        |
|     后端核心     | Spring Boot 2.7+、Spring MVC、Spring Security、MyBatis Plus  |   快速构建 RESTful API，简化 CRUD    |
|     数据存储     |                 MySQL 、Redis、本地文件系统                  | 结构化数据 + 热点缓存 + 静态资源存储 |
|     API 设计     |         RESTful 风格、Swagger/OpenAPI、统一响应格式          |       标准化接口，自动生成文档       |
|     AI 集成      |                         硅基流动 API                         |      智能问答、内容拓展场景支撑      |

## 二、系统功能模块设计概要

| 模块名称    | 优先级 | 核心定位                                                     | 关键子功能                                                   |
| ----------- | ------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 时间轴      | ★★★★★  | 系统核心载体，以时间为主线串联历史事件，构建 “时间 - 事件 - 人物 - 典故” 的核心科普链路 | 基础时间轴展示（缩放 / 拖拽 / 事件节点标注）、事件精准定位（按朝代 / 时间区间 / 事件类型筛选、关键词搜索）、时间轴联动（点击事件节点加载关联人物 / 典故科普面板、多事件节点对比标记） |
| 事件科普    | ★★★★★  | 时间轴核心补充，承载历史事件的核心科普信息                   | 事件基础信息展示（发生时间 / 背景 / 过程 / 影响）、事件关联维度展示（关联人物列表、关联典故列表、关联朝代背景）、事件数据可视化（事件时间线对比、影响范围示意图） |
| 人物科普    | ★★★★★  | 实现 “事件 - 人物” 强关联，聚焦历史人物专项科普              | 人物列表展示（按朝代 / 事件 / 身份类型分类、按生卒年排序）、人物详情卡（生平履历、核心事迹、关联事件 / 典故）、人物标签检索（按成就 / 身份 / 朝代等标签快速筛选） |
| 典故科普    | ★★★★★  | 实现 “人物 - 典故” 强关联，聚焦经典典故专项科普              | 典故列表展示（按朝代 / 人物 / 主题类型 / 难度等级分类）、典故详情卡（出处 / 原文 / 释义 / 寓意）、典故溯源展示（关联人物 / 事件 / 历史背景） |
| AI 科普助手 | ★★★★   | 赋能科普场景，提升用户科普体验与信息获取效率                 | 智能问答（事件 / 人物 / 典故相关问题解答）、内容拓展（基于科普内容生成延伸解读 / 趣味知识点）、个性化推荐（根据用户浏览轨迹推荐相关科普内容） |
| 学习工具    | ★★★    | 辅助科普内容沉淀，满足用户收藏 / 记录 / 导出需求             | 本地收藏（按事件 / 人物 / 典故分类收藏科普内容）、本地笔记（富文本编辑记录科普要点 / 心得）、本地导出（将科普内容批量导出为 Word/PDF 格式） |
| 趣味互动    | ★★★    | 增强科普趣味性，提升用户学习粘性                             | 典故答题挑战（基于典故科普内容出题）、人物事件匹配游戏（关联人物与对应历史事件）、历史科普小测验（覆盖事件 / 人物 / 典故核心知识点） |
| 系统管理    | ★★★    | 保障科普系统易用性与个性化，支撑系统稳定运行                 | 管理端（用户权限管理、科普数据维护 / 审核、系统参数配置）    |

## 三、系统数据库表设计概要

| 表名                                   | 核心用途                                                     | 主键        | 核心外键                                     | 关键字段（非外键）                                           |
| -------------------------------------- | ------------------------------------------------------------ | ----------- | -------------------------------------------- | ------------------------------------------------------------ |
| t_dynasty（朝代表）                    | 存储朝代基础信息，为事件 / 人物 / 典故科普提供朝代维度支撑   | dynasty_id  | -                                            | dynasty_name、start_year、end_year、capital、founder、map_path、dynasty_desc（朝代简介）等 |
| t_historical_event（历史事件表）       | 存储核心历史事件信息，作为时间轴科普的核心数据载体           | event_id    | dynasty_id                                   | event_name、event_year、event_bg（事件背景）、event_process（事件过程）、event_impact（事件影响）、event_type（事件类型）、event_pic_path（事件配图路径）等 |
| t_historical_person（历史人物表）      | 存储历史人物信息，支撑人物专项科普及 “事件 - 人物” 关联科普  | person_id   | dynasty_id                                   | person_name、alias、birth_year、death_year、identity_type（身份类型）、tags（人物标签）、avatar_path、person_bio（人物生平）、person_achievement（人物成就）等 |
| t_event_person（事件 - 人物关联表）    | 实现事件与人物多对多关联，支撑 “事件 - 人物” 联动科普        | ep_id       | event_id、person_id                          | relation_type（关联类型：主导 / 参与 / 受影响等）、relation_desc（关联描述）等 |
| t_allusion（经典典故表）               | 存储经典典故信息，支撑典故专项科普                           | allusion_id | dynasty_id                                   | allusion_name、source（出处）、original_text（原文）、paraphrase（释义）、moral（寓意）、theme_type（主题类型）、difficulty_level（难度等级）、allusion_desc（典故解读）等 |
| t_person_allusion（人物 - 典故关联表） | 实现人物与典故多对多关联，支撑 “人物 - 典故” 联动科普        | pa_id       | person_id、allusion_id                       | relation_desc（关联描述：典故主人公 / 典故相关人物等）、relation_bg（关联背景）等 |
| t_event_allusion（事件 - 典故关联表）  | 实现事件与典故多对多关联，完善 “事件 - 人物 - 典故” 科普链路 | ea_id       | event_id、allusion_id                        | relation_desc（关联描述：典故源于该事件 / 典故反映该事件背景等） |
| t_dynasty_stage（朝代细分阶段表）      | 存储长跨度朝代细分阶段，细化事件 / 人物 / 典故的朝代归属     | stage_id    | dynasty_id                                   | stage_name、start_year、end_year、stage_feature（阶段特征）、stage_desc（阶段简介）等 |
| t_collection（收藏表）                 | 存储用户收藏信息，支撑科普内容收藏功能                       | collect_id  | -                                            | collect_type（收藏类型：事件 / 人物 / 典故）、target_id（对应事件 / 人物 / 典故 ID）、folder_name、collect_time、user_id（用户 ID）等 |
| t_note（笔记表）                       | 存储用户笔记信息，支撑科普内容记录功能                       | note_id     | -                                            | note_type（笔记类型：事件 / 人物 / 典故）、target_id（对应事件 / 人物 / 典故 ID）、note_title、note_content（富文本内容）、create_time、update_time、user_id 等 |
| t_system_setting（系统设置表）         | 存储系统个性化设置，保障科普系统易用性                       | setting_id  | -                                            | setting_key、setting_value、setting_desc、user_id（用户级设置关联用户 ID，系统级设置为空）等 |
| t_question（答题题库表）               | 存储趣味互动答题题目，支撑科普趣味互动场景                   | question_id | dynasty_id、event_id、person_id、allusion_id | question_type（题目类型：单选 / 多选 / 问答）、question_content、options（选项，JSON 格式）、correct_answer、analysis（答案解析）、difficulty_level 等 |
| t_ai_interaction（AI 互动记录表）      | 存储 AI 科普助手的互动记录，支撑 AI 科普场景追溯与优化       | ai_id       | user_id                                      | interaction_type（互动类型：问答 / 内容拓展 / 推荐）、input_content（用户输入内容）、output_content（AI 输出内容）、interaction_time、target_type（关联科普类型：事件 / 人物 / 典故）、target_id 等 |
>>>>>>> 822a5594a4ae73b06db09ca8f2e5db1ca1651bf5
