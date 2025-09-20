-- ======================================
-- Dawn Blog 测试数据生成脚本
-- ======================================
USE dawn;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ======================================
-- 1. 用户测试数据
-- ======================================

-- 生成用户信息
INSERT INTO `t_user_info` (`email`, `nickname`, `avatar`, `intro`, `website`, `is_subscribe`, `is_disable`, `create_time`) VALUES
('user1@test.com', '张三', 'https://picsum.photos/100/100?random=1', '热爱编程的后端开发工程师', 'https://zhangsan.dev', 1, 0, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY)),
('user2@test.com', '李四', 'https://picsum.photos/100/100?random=2', '专注前端技术的UI设计师', 'https://lisi.design', 0, 0, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 300) DAY)),
('user3@test.com', '王五', 'https://picsum.photos/100/100?random=3', '全栈开发者，喜欢分享技术心得', 'https://wangwu.tech', 1, 0, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 200) DAY)),
('user4@test.com', '赵六', 'https://picsum.photos/100/100?random=4', 'DevOps工程师，云原生技术爱好者', 'https://zhaoliu.cloud', 0, 0, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 150) DAY)),
('user5@test.com', '钱七', 'https://picsum.photos/100/100?random=5', '数据科学家，AI算法研究员', 'https://qianqi.ai', 1, 0, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 100) DAY));

-- 生成用户认证信息
INSERT INTO `t_user_auth` (`user_info_id`, `username`, `password`, `login_type`, `ip_address`, `ip_source`, `create_time`, `last_login_time`) VALUES
(2, 'user1@test.com', '$2a$10$/Z90STxVyGOIfNhTfvzbEuJ9t1yHjrkN6pBMRAqd5g5SdNIrdt5Da', 1, '192.168.1.101', '中国|广东省|深圳市|电信', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'user2@test.com', '$2a$10$/Z90STxVyGOIfNhTfvzbEuJ9t1yHjrkN6pBMRAqd5g5SdNIrdt5Da', 1, '192.168.1.102', '中国|北京市|北京市|联通', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 'user3@test.com', '$2a$10$/Z90STxVyGOIfNhTfvzbEuJ9t1yHjrkN6pBMRAqd5g5SdNIrdt5Da', 1, '192.168.1.103', '中国|上海市|上海市|移动', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 'user4@test.com', '$2a$10$/Z90STxVyGOIfNhTfvzbEuJ9t1yHjrkN6pBMRAqd5g5SdNIrdt5Da', 1, '192.168.1.104', '中国|浙江省|杭州市|电信', DATE_SUB(NOW(), INTERVAL 40 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
(6, 'user5@test.com', '$2a$10$/Z90STxVyGOIfNhTfvzbEuJ9t1yHjrkN6pBMRAqd5g5SdNIrdt5Da', 1, '192.168.1.105', '中国|江苏省|苏州市|移动', DATE_SUB(NOW(), INTERVAL 50 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY));

-- 分配用户角色（都是普通用户）
INSERT INTO `t_user_role` (`user_id`, `role_id`) VALUES 
(2, 2), (3, 2), (4, 2), (5, 2), (6, 2);

-- ======================================
-- 2. 文章测试数据
-- ======================================

-- 批量生成文章数据
INSERT INTO `t_article` (`user_id`, `category_id`, `article_cover`, `article_title`, `article_content`, `is_top`, `is_featured`, `is_delete`, `status`, `type`, `create_time`, `update_time`) VALUES
-- 技术开发类文章
(1, 1, 'https://picsum.photos/800/400?random=101', 'Spring Boot 3.0 新特性全面解析', '# Spring Boot 3.0 新特性全面解析\n\nSpring Boot 3.0 是一个重要的里程碑版本，带来了众多令人兴奋的新特性...\n\n## 主要变化\n\n### 1. Java 17 基线要求\nSpring Boot 3.0 要求 Java 17 作为最低版本，这意味着可以充分利用 Java 的新特性...\n\n### 2. 原生镜像支持\n通过 GraalVM 原生镜像支持，应用启动时间大幅减少...\n\n### 3. 可观测性改进\n集成了 Micrometer Tracing，提供分布式追踪能力...', 1, 1, 0, 1, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),

(2, 2, 'https://picsum.photos/800/400?random=102', 'Vue 3 Composition API 最佳实践', '# Vue 3 Composition API 最佳实践\n\nVue 3 的 Composition API 为我们提供了更灵活的代码组织方式...\n\n## 为什么使用 Composition API\n\n- 更好的逻辑复用\n- 更好的类型推断\n- 更灵活的代码组织\n\n```javascript\nimport { ref, computed, onMounted } from "vue"\n\nexport default {\n  setup() {\n    const count = ref(0)\n    const doubleCount = computed(() => count.value * 2)\n    \n    return { count, doubleCount }\n  }\n}\n```', 0, 1, 0, 1, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

(3, 3, 'https://picsum.photos/800/400?random=103', 'Node.js 性能优化实战指南', '# Node.js 性能优化实战指南\n\n在高并发的 Web 应用中，Node.js 性能优化至关重要...\n\n## 内存优化\n\n### 1. 避免内存泄漏\n```javascript\n// 错误示例\nconst cache = {}\nsetInterval(() => {\n  cache[Date.now()] = new Array(1000000)\n}, 100)\n\n// 正确示例  \nconst LRU = require("lru-cache")\nconst cache = new LRU({ max: 500 })\n```\n\n### 2. 使用流处理大文件', 0, 0, 0, 1, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), NULL),

(4, 4, 'https://picsum.photos/800/400?random=104', 'MySQL 8.0 性能调优深度解析', '# MySQL 8.0 性能调优深度解析\n\nMySQL 8.0 带来了很多性能改进，本文将深入探讨如何进行性能调优...\n\n## 索引优化\n\n### 复合索引的最左前缀原则\n```sql\n-- 创建复合索引\nCREATE INDEX idx_user_status_time ON t_article(user_id, status, create_time);\n\n-- 可以利用索引的查询\nSELECT * FROM t_article WHERE user_id = 1 AND status = 1;\nSELECT * FROM t_article WHERE user_id = 1;\n```', 0, 1, 0, 1, 1, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

(1, 5, 'https://picsum.photos/800/400?random=105', 'Docker 容器化部署实践', '# Docker 容器化部署实践\n\n容器化部署已经成为现代应用部署的标准方式...\n\n## Dockerfile 最佳实践\n\n```dockerfile\nFROM openjdk:17-jre-slim\n\nWORKDIR /app\nCOPY target/app.jar app.jar\n\nEXPOSE 8080\nCMD ["java", "-jar", "app.jar"]\n```\n\n## Docker Compose 配置\n\n```yaml\nversion: "3.8"\nservices:\n  app:\n    build: .\n    ports:\n      - "8080:8080"\n  mysql:\n    image: mysql:8.0\n    environment:\n      MYSQL_ROOT_PASSWORD: password\n```', 1, 0, 0, 1, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), NULL),

-- 生成更多文章（使用循环方式）
(2, 6, 'https://picsum.photos/800/400?random=106', '深入理解算法：二叉树遍历详解', '# 深入理解算法：二叉树遍历详解\n\n二叉树是计算机科学中最基础的数据结构之一...\n\n## 前序遍历\n\n```java\npublic void preOrder(TreeNode root) {\n    if (root == null) return;\n    System.out.println(root.val);\n    preOrder(root.left);\n    preOrder(root.right);\n}\n```\n\n时间复杂度：O(n)\n空间复杂度：O(h)，h为树的高度', 0, 0, 0, 1, 1, DATE_SUB(NOW(), INTERVAL 6 DAY), NULL),

(3, 7, 'https://picsum.photos/800/400?random=107', '从零搭建微服务架构', '# 从零搭建微服务架构\n\n微服务架构将单体应用拆分为多个小服务...\n\n## 服务注册与发现\n\n使用 Eureka 实现服务注册：\n\n```java\n@EnableEurekaServer\n@SpringBootApplication\npublic class EurekaServerApplication {\n    public static void main(String[] args) {\n        SpringApplication.run(EurekaServerApplication.class, args);\n    }\n}\n```', 0, 1, 0, 1, 1, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),

(4, 8, 'https://picsum.photos/800/400?random=108', '我的学习方法论分享', '# 我的学习方法论分享\n\n作为一名程序员，持续学习是必不可少的...\n\n## 费曼学习法\n\n1. 选择要学习的概念\n2. 用简单的话解释给别人听\n3. 识别知识空白并回到原材料\n4. 简化和使用类比\n\n## 刻意练习\n\n- 设定明确目标\n- 专注于弱点\n- 获得即时反馈\n- 重复练习', 0, 0, 0, 1, 1, DATE_SUB(NOW(), INTERVAL 8 DAY), NULL),

(1, 9, 'https://picsum.photos/800/400?random=109', '周末登山记：拥抱自然的力量', '# 周末登山记：拥抱自然的力量\n\n这个周末选择了去爬泰山，感受日出的壮丽...\n\n## 登山准备\n\n- 登山鞋：选择防滑耐磨的\n- 登山杖：减轻膝盖压力\n- 保温杯：山顶温度较低\n- 头灯：凌晨登山必备\n\n## 登山感悟\n\n每一步的坚持，都是对自己的超越。当站在山顶看日出的那一刻，所有的疲惫都化作了内心的宁静与满足。\n\n生活如登山，路虽崎岖，但风景在高处。', 0, 0, 0, 1, 1, DATE_SUB(NOW(), INTERVAL 9 DAY), NULL),

(2, 10, 'https://picsum.photos/800/400?random=110', '《代码整洁之道》读书笔记', '# 《代码整洁之道》读书笔记\n\nRobert C. Martin 的这本书是每个程序员都应该读的经典...\n\n## 核心观点\n\n### 1. 有意义的命名\n\n```java\n// 不好的命名\nint d; // 经过的时间（天）\n\n// 好的命名\nint daysSinceCreation;\nint daysSinceModification;\nint fileAgeInDays;\n```\n\n### 2. 函数应该短小\n\n- 函数的第一规则是要短小\n- 第二条规则是还要更短小\n- 函数应该只做一件事', 0, 1, 0, 1, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY));

-- 再生成一批文章数据
INSERT INTO `t_article` (`user_id`, `category_id`, `article_cover`, `article_title`, `article_content`, `is_top`, `is_featured`, `is_delete`, `status`, `type`, `create_time`, `update_time`) VALUES
(3, 11, 'https://picsum.photos/800/400?random=111', '程序员职场晋升心得', '# 程序员职场晋升心得\n\n从初级开发到高级工程师的成长路径...\n\n## 技术能力\n\n- 深度：在某个领域深入研究\n- 广度：了解相关技术栈\n- 架构思维：从业务角度思考技术方案\n\n## 软技能\n\n- 沟通协调能力\n- 项目管理能力\n- 团队协作能力', 0, 0, 0, 1, 1, DATE_SUB(NOW(), INTERVAL 11 DAY), NULL),

(4, 12, 'https://picsum.photos/800/400?random=112', 'Rust 语言入门指南', '# Rust 语言入门指南\n\nRust 是一门系统编程语言，专注于安全、速度和并发...\n\n## 所有权系统\n\n```rust\nfn main() {\n    let s1 = String::from("hello");\n    let s2 = s1; // s1 被移动到 s2\n    // println!("{}", s1); // 这里会编译错误\n    println!("{}", s2);\n}\n```\n\n## 借用和引用\n\n```rust\nfn calculate_length(s: &String) -> usize {\n    s.len()\n}\n```', 0, 1, 0, 2, 1, DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),

(1, 1, 'https://picsum.photos/800/400?random=113', 'GraphQL 与 REST API 对比分析', '# GraphQL 与 REST API 对比分析\n\n现代 Web 开发中，API 设计至关重要...\n\n## REST API 特点\n\n- 资源导向\n- 无状态\n- 统一接口\n- 分层系统\n\n## GraphQL 优势\n\n- 按需获取数据\n- 强类型系统\n- 单一端点\n- 实时订阅', 0, 0, 0, 1, 2, DATE_SUB(NOW(), INTERVAL 13 DAY), NULL),

(2, 2, 'https://picsum.photos/800/400?random=114', 'React 18 并发特性详解', '# React 18 并发特性详解\n\nReact 18 引入了并发渲染，提升了用户体验...\n\n## Suspense 改进\n\n```jsx\nfunction App() {\n  return (\n    <Suspense fallback={<Loading />}>\n      <ProfilePage />\n    </Suspense>\n  );\n}\n```\n\n## useTransition Hook\n\n```jsx\nfunction App() {\n  const [isPending, startTransition] = useTransition();\n  \n  const handleClick = () => {\n    startTransition(() => {\n      setTab("posts");\n    });\n  };\n}\n```', 0, 1, 0, 1, 1, DATE_SUB(NOW(), INTERVAL 14 DAY), NULL),

(3, 3, 'https://picsum.photos/800/400?random=115', 'TypeScript 高级类型应用', '# TypeScript 高级类型应用\n\nTypeScript 的类型系统非常强大...\n\n## 条件类型\n\n```typescript\ntype ApiResponse<T> = T extends string\n  ? { message: T }\n  : { data: T };\n\ntype StringResponse = ApiResponse<string>; // { message: string }\ntype DataResponse = ApiResponse<User>; // { data: User }\n```\n\n## 映射类型\n\n```typescript\ntype Partial<T> = {\n  [P in keyof T]?: T[P];\n};\n```', 0, 0, 0, 3, 1, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY));

-- ======================================
-- 3. 文章标签关联数据
-- ======================================

-- 为文章添加标签
INSERT INTO `t_article_tag` (`article_id`, `tag_id`) VALUES
-- Spring Boot 文章的标签
(1, 1), (1, 6), (1, 15),  -- Java, Spring Boot, 微服务
-- Vue 3 文章的标签  
(2, 2), (2, 4), (2, 23),  -- JavaScript, Vue.js, 最佳实践
-- Node.js 文章的标签
(3, 2), (3, 7), (3, 18),  -- JavaScript, Node.js, 性能优化
-- MySQL 文章的标签
(4, 8), (4, 18), (4, 20), -- MySQL, 性能优化, 数据结构
-- Docker 文章的标签
(5, 10), (5, 23), (5, 24), -- Docker, 最佳实践, 代码重构
-- 算法文章的标签
(6, 20), (6, 21), (6, 19), -- 数据结构, 算法, 设计模式
-- 微服务文章的标签
(7, 1), (7, 15), (7, 16),  -- Java, 微服务, 分布式
-- 学习方法文章的标签
(8, 27), (8, 28), (8, 30), -- 学习方法, 职业规划, 读书笔记
-- 登山文章的标签
(9, 31), (9, 32), (9, 34), -- 生活感悟, 旅行, 健身
-- 读书笔记文章的标签
(10, 30), (10, 23), (10, 24), -- 读书笔记, 最佳实践, 代码重构
-- 职场文章的标签
(11, 28), (11, 29), (11, 27), -- 职业规划, 时间管理, 学习方法
-- Rust 文章的标签
(12, 3), (12, 18), (12, 17), -- Python, 性能优化, 高并发
-- GraphQL 文章的标签
(13, 26), (13, 23), (13, 2), -- API设计, 最佳实践, JavaScript
-- React 文章的标签
(14, 2), (14, 5), (14, 18),  -- JavaScript, React, 性能优化
-- TypeScript 文章的标签
(15, 2), (15, 19), (15, 23); -- JavaScript, 设计模式, 最佳实践

-- ======================================
-- 4. 评论测试数据
-- ======================================

-- 文章评论
INSERT INTO `t_comment` (`user_id`, `topic_id`, `comment_content`, `reply_user_id`, `parent_id`, `type`, `is_delete`, `is_review`, `create_time`) VALUES
(2, 1, '这篇文章写得很详细，Spring Boot 3.0 的新特性确实很强大！', NULL, NULL, 1, 0, 1, DATE_SUB(NOW(), INTERVAL 12 HOUR)),
(3, 1, '原生镜像支持这个特性我很期待，可以大大减少启动时间', NULL, NULL, 1, 0, 1, DATE_SUB(NOW(), INTERVAL 10 HOUR)),
(4, 1, '请问作者有没有实际的性能测试数据对比？', NULL, NULL, 1, 0, 1, DATE_SUB(NOW(), INTERVAL 8 HOUR)),
(1, 1, '我会在后续文章中补充性能测试的详细数据', 4, 3, 1, 0, 1, DATE_SUB(NOW(), INTERVAL 6 HOUR)),

(3, 2, 'Composition API 确实比 Options API 更灵活，组织代码更清晰', NULL, NULL, 1, 0, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 2, '示例代码很实用，已经在项目中应用了', NULL, NULL, 1, 0, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 2, '希望能出一个完整的项目实战教程', NULL, NULL, 1, 0, 1, DATE_SUB(NOW(), INTERVAL 20 HOUR)),

(2, 4, 'MySQL 索引优化这块内容很实用，解决了我项目中的性能问题', NULL, NULL, 1, 0, 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 4, '复合索引的最左前缀原则确实很重要，经常被忽略', NULL, NULL, 1, 0, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- 留言板评论
(3, NULL, '博客做得很棒，学到了很多东西！', NULL, NULL, 2, 0, 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, NULL, '希望能多分享一些实战项目的经验', NULL, NULL, 2, 0, 1, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, NULL, '期待更多关于微服务架构的文章', NULL, NULL, 2, 0, 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- 关于我页面评论
(2, NULL, '作者的技术栈很全面，向您学习！', NULL, NULL, 3, 0, 1, DATE_SUB(NOW(), INTERVAL 6 DAY)),

-- 友链评论
(3, NULL, '可以交换友链吗？我的博客地址是 https://example.com', NULL, NULL, 4, 0, 0, DATE_SUB(NOW(), INTERVAL 7 DAY));

-- ======================================
-- 5. 说说测试数据
-- ======================================

INSERT INTO `t_talk` (`user_id`, `content`, `images`, `is_top`, `status`, `create_time`) VALUES
(1, '今天完成了博客系统的重构，使用了最新的Spring Boot 3.0，性能提升了30%！🚀', '["https://picsum.photos/400/300?random=201", "https://picsum.photos/400/300?random=202"]', 1, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(1, '分享一个调试技巧：使用 JProfiler 分析内存泄漏问题，发现了一个隐藏很深的 bug', NULL, 0, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, '周末爬山归来，在山顶看日出的感觉真是太棒了！生活不止代码，还有诗和远方 ✨', '["https://picsum.photos/400/300?random=203", "https://picsum.photos/400/300?random=204", "https://picsum.photos/400/300?random=205"]', 0, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, '正在学习 Rust 语言，所有权系统的概念很有趣，虽然学习曲线陡峭但很有意思', NULL, 0, 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, '推荐一本书：《代码整洁之道》，每读一遍都有新的感悟', '["https://picsum.photos/300/400?random=206"]', 0, 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, '今天的午餐：自制的番茄鸡蛋面，编程之余也要好好生活 🍜', '["https://picsum.photos/400/300?random=207"]', 0, 1, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(1, '分享一个有趣的算法：布隆过滤器，用极小的空间判断元素是否存在，虽然有一定误判率但在某些场景下非常有用', NULL, 0, 2, DATE_SUB(NOW(), INTERVAL 10 DAY));

-- ======================================
-- 6. 友情链接测试数据
-- ======================================

INSERT INTO `t_friend_link` (`link_name`, `link_avatar`, `link_address`, `link_intro`, `create_time`) VALUES
('技术博客导航', 'https://picsum.photos/100/100?random=301', 'https://nav.tech-blog.com', '收录优质技术博客的导航网站', DATE_SUB(NOW(), INTERVAL 30 DAY)),
('开源中国', 'https://picsum.photos/100/100?random=302', 'https://www.oschina.net', '中国最大的开源技术社区', DATE_SUB(NOW(), INTERVAL 25 DAY)),
('掘金社区', 'https://picsum.photos/100/100?random=303', 'https://juejin.cn', '一个帮助开发者成长的社区', DATE_SUB(NOW(), INTERVAL 20 DAY)),
('GitHub', 'https://picsum.photos/100/100?random=304', 'https://github.com', '全球最大的代码托管平台', DATE_SUB(NOW(), INTERVAL 15 DAY)),
('Stack Overflow', 'https://picsum.photos/100/100?random=305', 'https://stackoverflow.com', '程序员问答社区', DATE_SUB(NOW(), INTERVAL 10 DAY)),
('MDN Web Docs', 'https://picsum.photos/100/100?random=306', 'https://developer.mozilla.org', 'Web 开发权威文档', DATE_SUB(NOW(), INTERVAL 5 DAY));

-- ======================================
-- 7. 相册和照片测试数据
-- ======================================

INSERT INTO `t_photo_album` (`album_name`, `album_desc`, `album_cover`, `is_delete`, `status`, `create_time`) VALUES
('代码生活', '记录编程日常的点点滴滴', 'https://picsum.photos/300/200?random=401', 0, 1, DATE_SUB(NOW(), INTERVAL 60 DAY)),
('旅行足迹', '走过的路，看过的风景', 'https://picsum.photos/300/200?random=402', 0, 1, DATE_SUB(NOW(), INTERVAL 50 DAY)),
('美食记录', '生活需要仪式感', 'https://picsum.photos/300/200?random=403', 0, 2, DATE_SUB(NOW(), INTERVAL 40 DAY)),
('技术分享', '技术会议和学习记录', 'https://picsum.photos/300/200?random=404', 0, 1, DATE_SUB(NOW(), INTERVAL 30 DAY));

-- 为相册添加照片
INSERT INTO `t_photo` (`album_id`, `photo_name`, `photo_desc`, `photo_src`, `is_delete`, `create_time`) VALUES
-- 代码生活相册
(1, '工作环境', '我的编程工作台', 'https://picsum.photos/600/400?random=501', 0, DATE_SUB(NOW(), INTERVAL 55 DAY)),
(1, '新书到货', '刚买的技术书籍', 'https://picsum.photos/600/400?random=502', 0, DATE_SUB(NOW(), INTERVAL 45 DAY)),
(1, '代码评审', '团队代码评审现场', 'https://picsum.photos/600/400?random=503', 0, DATE_SUB(NOW(), INTERVAL 35 DAY)),
(1, '加班日常', '深夜还在调试bug', 'https://picsum.photos/600/400?random=504', 0, DATE_SUB(NOW(), INTERVAL 25 DAY)),

-- 旅行足迹相册
(2, '泰山日出', '登泰山看日出', 'https://picsum.photos/600/400?random=505', 0, DATE_SUB(NOW(), INTERVAL 40 DAY)),
(2, '西湖美景', '杭州西湖游玩', 'https://picsum.photos/600/400?random=506', 0, DATE_SUB(NOW(), INTERVAL 35 DAY)),
(2, '海边漫步', '青岛海边的夕阳', 'https://picsum.photos/600/400?random=507', 0, DATE_SUB(NOW(), INTERVAL 20 DAY)),

-- 美食记录相册
(3, '自制咖啡', '手冲咖啡的乐趣', 'https://picsum.photos/600/400?random=508', 0, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(3, '周末大餐', '给自己做的丰盛午餐', 'https://picsum.photos/600/400?random=509', 0, DATE_SUB(NOW(), INTERVAL 10 DAY)),

-- 技术分享相册
(4, 'Spring 大会', 'Spring 技术大会现场', 'https://picsum.photos/600/400?random=510', 0, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(4, '团队技术分享', '给团队分享微服务架构', 'https://picsum.photos/600/400?random=511', 0, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(4, '开源项目展示', '展示自己的开源项目', 'https://picsum.photos/600/400?random=512', 0, DATE_SUB(NOW(), INTERVAL 5 DAY));

-- ======================================
-- 8. 访问统计数据
-- ======================================

-- 生成最近30天的访问统计数据
INSERT INTO `t_unique_view` (`views_count`, `create_time`) 
SELECT 
    FLOOR(RAND() * 200 + 50) as views_count,
    DATE_SUB(CURDATE(), INTERVAL seq DAY) as create_time
FROM (
    SELECT 0 as seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION
    SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION
    SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29
) as seq_table
ORDER BY create_time;

-- ======================================
-- 9. 操作日志测试数据
-- ======================================

INSERT INTO `t_operation_log` (`opt_module`, `opt_type`, `opt_uri`, `opt_method`, `opt_desc`, `request_param`, `request_method`, `response_data`, `user_id`, `nickname`, `ip_address`, `ip_source`, `create_time`) VALUES
('文章模块', '新增', '/admin/articles', 'save', '保存文章', '{"title":"Spring Boot 3.0 新特性","content":"..."}', 'POST', '{"code":200,"message":"操作成功"}', 1, 'Supremes', '192.168.1.100', '中国|江苏省|南京市|移动', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('分类模块', '新增', '/admin/categories', 'save', '保存分类', '{"categoryName":"新技术"}', 'POST', '{"code":200,"message":"操作成功"}', 1, 'Supremes', '192.168.1.100', '中国|江苏省|南京市|移动', DATE_SUB(NOW(), INTERVAL 2 DAY)),
('标签模块', '新增', '/admin/tags', 'save', '保存标签', '{"tagName":"Spring Boot 3"}', 'POST', '{"code":200,"message":"操作成功"}', 1, 'Supremes', '192.168.1.100', '中国|江苏省|南京市|移动', DATE_SUB(NOW(), INTERVAL 3 DAY)),
('用户模块', '修改', '/admin/users/disable', 'update', '修改用户禁用状态', '{"userIdList":[2],"isDisable":0}', 'PUT', '{"code":200,"message":"操作成功"}', 1, 'Supremes', '192.168.1.100', '中国|江苏省|南京市|移动', DATE_SUB(NOW(), INTERVAL 4 DAY)),
('评论模块', '审核', '/admin/comments/review', 'update', '审核评论', '{"commentIdList":[1,2,3],"isReview":1}', 'PUT', '{"code":200,"message":"操作成功"}', 1, 'Supremes', '192.168.1.100', '中国|江苏省|南京市|移动', DATE_SUB(NOW(), INTERVAL 5 DAY));

-- ======================================
-- 10. 定时任务日志测试数据
-- ======================================

INSERT INTO `t_job_log` (`job_id`, `job_name`, `job_group`, `invoke_target`, `job_message`, `status`, `exception_info`, `create_time`, `start_time`, `end_time`) VALUES
(81, '统计用户地域分布', '默认', 'dawnQuartz.statisticalUserArea', '统计用户地域分布任务执行成功', 0, '', DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 31 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(82, '统计访问量', '默认', 'dawnQuartz.saveUniqueView', '保存访问量统计成功，今日访问量：156', 0, '', DATE_SUB(NOW(), INTERVAL 10 MINUTE), DATE_SUB(NOW(), INTERVAL 11 MINUTE), DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
(83, '清空redis访客记录', '默认', 'dawnQuartz.clear', '清空Redis访客记录成功', 0, '', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 61 MINUTE), DATE_SUB(NOW(), INTERVAL 60 MINUTE)),
(85, '清理定时任务日志', '默认', 'dawnQuartz.clearJobLogs', '清理7天前的任务日志，共清理128条记录', 0, '', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1440 MINUTE), DATE_SUB(NOW(), INTERVAL 1439 MINUTE));

COMMIT;
SET FOREIGN_KEY_CHECKS = 1;

-- ======================================
-- 数据统计查询（可选执行，用于验证数据）
-- ======================================

-- 统计生成的数据量
-- SELECT 'users' as table_name, COUNT(*) as count FROM t_user_info
-- UNION ALL
-- SELECT 'articles', COUNT(*) FROM t_article
-- UNION ALL  
-- SELECT 'comments', COUNT(*) FROM t_comment
-- UNION ALL
-- SELECT 'tags', COUNT(*) FROM t_tag
-- UNION ALL
-- SELECT 'talks', COUNT(*) FROM t_talk
-- UNION ALL
-- SELECT 'photos', COUNT(*) FROM t_photo;

-- 生成完成提示
SELECT '🎉 测试数据生成完成！' as message;
SELECT '包含：用户、文章、评论、说说、相册、友链等测试数据' as description;
