INSERT INTO course (id, name, description) VALUES
(1, 'Java程序设计', '面向 Java 初学者，覆盖语法、集合、面向对象和并发基础'),
(2, '数据结构与算法', '覆盖数组、链表、栈、队列、树、图、排序和检索'),
(3, '计算机网络', '覆盖网络分层、TCP/IP、HTTP 与常见协议'),
(4, '数据库系统', '覆盖关系模型、SQL、索引、事务与数据库设计');

INSERT INTO resource_item (id, course_id, title, type, content) VALUES
(1, 1, 'Java 基础语法讲义', 'document', 'Java 是面向对象的编程语言，程序由类组成。main 方法是程序入口。基本数据类型包括 int、double、boolean、char 等。'),
(2, 1, 'Java 集合框架', 'document', 'Java 集合框架包含 List、Set、Map 等接口。ArrayList 基于动态数组，LinkedList 基于双向链表，HashMap 基于哈希表。'),
(3, 2, '排序算法总结', 'document', '常见排序算法包括冒泡排序、插入排序、归并排序、快速排序和堆排序。快速排序平均时间复杂度为 O(n log n)。'),
(4, 2, '二叉树基础', 'document', '二叉树每个节点最多有两个子节点。二叉搜索树左子树小于根节点，右子树大于根节点，适合快速查找。'),
(5, 3, 'TCP/IP 分层模型', 'document', 'TCP/IP 模型分为应用层、传输层、网络层和网络接口层。TCP 提供可靠连接，UDP 提供无连接传输。'),
(6, 3, 'HTTP 协议', 'document', 'HTTP 是应用层协议，常见方法包括 GET、POST、PUT、DELETE。HTTPS 通过 TLS 提供加密传输。'),
(7, 4, 'SQL 基础', 'document', 'SQL 用于操作关系数据库，常用语句包括 SELECT、INSERT、UPDATE、DELETE。WHERE 用于条件过滤，JOIN 用于多表关联。'),
(8, 4, '事务与索引', 'document', '事务具有 ACID 特性：原子性、一致性、隔离性、持久性。索引可以加速查询，但会占用存储并增加写入成本。');

INSERT INTO knowledge_chunk (id, course_id, title, content, tags) VALUES
(1, 1, 'Java 程序入口', 'Java 程序的入口是 main 方法，格式为 public static void main(String[] args)。每个可执行类都可以定义 main 方法。', 'Java,main'),
(2, 1, '面向对象三大特性', 'Java 面向对象包括封装、继承和多态。封装通过 private 隐藏内部状态，继承通过 extends 复用父类，多态通过方法重写实现。', 'Java,面向对象'),
(3, 1, 'HashMap 原理', 'HashMap 基于哈希表实现，通过 hashCode 定位桶，通过 equals 处理冲突。JDK 8 中链表长度超过阈值时转换为红黑树。', 'Java,HashMap'),
(4, 2, '快速排序', '快速排序通过选取基准值把数组分成两部分，再递归排序。平均时间复杂度 O(n log n)，最坏情况 O(n^2)。', '排序,快速排序'),
(5, 2, '二叉搜索树', '二叉搜索树满足左子树小于根、右子树大于根。查找、插入和删除平均时间复杂度为 O(log n)，树退化为链时变为 O(n)。', '二叉树,查找'),
(6, 2, '动态规划', '动态规划通过把问题拆成重叠子问题，并用状态转移方程求解，常用于最优化问题，例如背包、最长公共子序列。', '算法,动态规划'),
(7, 3, 'TCP 三次握手', 'TCP 建立连接需要三次握手：客户端发送 SYN，服务端回复 SYN+ACK，客户端再发送 ACK。这用于确认双方收发能力。', 'TCP,网络'),
(8, 3, 'HTTP 状态码', 'HTTP 状态码中 2xx 表示成功，3xx 表示重定向，4xx 表示客户端错误，5xx 表示服务端错误。', 'HTTP,状态码'),
(9, 3, 'HTTPS 加密', 'HTTPS 使用 TLS 加密 HTTP 通信，通过证书验证服务器身份，并使用对称加密保护数据传输。', 'HTTPS,TLS'),
(10, 4, 'SQL JOIN', 'JOIN 用于关联多张表。INNER JOIN 只返回匹配行，LEFT JOIN 返回左表全部行和右表匹配行，RIGHT JOIN 相反。', 'SQL,JOIN'),
(11, 4, '事务 ACID', '事务保证 ACID：原子性要求全部成功或全部回滚，一致性保证数据约束，隔离性控制并发可见性，持久性保证提交后不丢失。', '事务,ACID'),
(12, 4, '索引原理', 'B+ 树索引适合范围查询和等值查询。索引可以提升查询性能，但会增加写入开销并占用额外空间。', '索引,B+树');

INSERT INTO question (id, course_id, stem, option_a, option_b, option_c, option_d, answer, analysis) VALUES
(1, 1, 'Java 程序的入口方法是什么？', 'main', 'start', 'run', 'init', 'A', 'Java 程序的入口是 public static void main(String[] args)。'),
(2, 1, 'HashMap 在 JDK 8 中解决哈希冲突的主要方式是什么？', '开放寻址', '链表 + 红黑树', '二次探测', '独立链表', 'B', 'JDK 8 的 HashMap 使用链表，链表过长时转换为红黑树。'),
(3, 2, '快速排序的平均时间复杂度是？', 'O(n)', 'O(n log n)', 'O(n^2)', 'O(log n)', 'B', '快速排序平均时间复杂度为 O(n log n)。'),
(4, 2, '二叉搜索树中，左子树所有节点值应满足什么条件？', '大于根节点', '小于根节点', '等于根节点', '无要求', 'B', '二叉搜索树左子树小于根节点，右子树大于根节点。'),
(5, 3, 'TCP 建立连接需要几次握手？', '1', '2', '3', '4', 'C', 'TCP 建立连接使用三次握手。'),
(6, 3, 'HTTP 状态码 404 表示什么？', '服务器错误', '资源未找到', '重定向', '请求成功', 'B', '404 表示请求的资源未找到。'),
(7, 4, '事务的 ACID 特性不包括哪一项？', '原子性', '一致性', '并发性', '持久性', 'C', 'ACID 包括原子性、一致性、隔离性和持久性。'),
(8, 4, 'INNER JOIN 返回的是？', '左表全部行', '右表全部行', '两表匹配行', '两表全部行', 'C', 'INNER JOIN 只返回两表匹配的行。');
