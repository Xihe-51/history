/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80033 (8.0.33)
 Source Host           : localhost:3306
 Source Schema         : history

 Target Server Type    : MySQL
 Target Server Version : 80033 (8.0.33)
 File Encoding         : 65001

 Date: 15/04/2026 13:06:08
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for allusion
-- ----------------------------
DROP TABLE IF EXISTS `allusion`;
CREATE TABLE `allusion`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '典故ID，主键',
  `allusion_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '典故名称',
  `source` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '出处（古籍名称）',
  `dynasty_id` bigint NULL DEFAULT NULL,
  `original_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '原文',
  `paraphrase` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '释义',
  `moral` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '寓意',
  `theme_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '其他' COMMENT '主题类型（励志/权谋/友情/忠诚/智慧/其他）',
  `multimedia_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '多媒体资源路径（图片/音频/视频）',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_allusion_name`(`allusion_name` ASC) USING BTREE COMMENT '按典故名称索引',
  INDEX `idx_allusion_theme`(`theme_type` ASC) USING BTREE COMMENT '按主题类型索引'
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '存储经典典故信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of allusion
-- ----------------------------
INSERT INTO `allusion` VALUES (1, '卧薪尝胆', '史记·越王勾践世家', 3, NULL, '刻苦自励，发奋图强', '忍辱负重，终能成功', '励志', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (2, '纸上谈兵', '史记·廉颇蔺相如列传', 4, NULL, '空谈理论，不解决实际问题', '实践出真知', '警示', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (3, '破釜沉舟', '史记·项羽本纪', 4, NULL, '下定决心，不留退路', '置之死地而后生', '励志', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (4, '草木皆兵', '晋书·苻坚载记', 7, NULL, '惊慌失措，疑神疑鬼', '保持冷静，不被表象迷惑', '警示', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (5, '四面楚歌', '史记·项羽本纪', 5, NULL, '陷入孤立无援的困境', '得道多助，失道寡助', '警示', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (6, '完璧归赵', '史记·廉颇蔺相如列传', 3, NULL, '完好无损地归还物品', '智勇双全，不辱使命', '智慧', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (7, '指鹿为马', '史记·秦始皇本纪', 4, NULL, '故意颠倒黑白，混淆是非', '坚持真理，不阿谀奉承', '警示', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (8, '负荆请罪', '史记·廉颇蔺相如列传', 3, NULL, '主动认错，请求责罚', '知错能改，善莫大焉', '励志', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (9, '悬梁刺股', '战国策·秦策一', 3, NULL, '刻苦学习', '勤奋是成功之母', '励志', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (10, '凿壁偷光', '西京杂记', 5, NULL, '勤奋学习，克服困难', '只要有决心，条件再差也能学习', '励志', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (11, '闻鸡起舞', '晋书·祖逖传', 7, NULL, '勤奋自励，奋发有为', '勤奋努力，终有成就', '励志', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (12, '程门立雪', '宋史·杨时传', 11, NULL, '尊师重道', '尊敬师长，虚心求教', '励志', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (13, '三顾茅庐', '三国志·蜀书·诸葛亮传', 6, NULL, '诚心诚意邀请', '诚心诚意，必能感动他人', '励志', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (14, '草船借箭', '三国演义', 6, NULL, '利用智慧达到目的', '善用智慧，借力打力', '智慧', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (15, '空城计', '三国演义', 6, NULL, '虚张声势，迷惑敌人', '临危不乱，以智取胜', '智慧', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (16, '乐不思蜀', '三国志·蜀书·后主传', 6, NULL, '乐而忘返', '居安思危，不忘根本', '警示', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (17, '望梅止渴', '世说新语·假谲', 6, NULL, '用空想安慰自己', '积极暗示，克服困难', '智慧', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (18, '画蛇添足', '战国策·齐策二', 3, NULL, '多此一举，弄巧成拙', '适可而止，过犹不及', '警示', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (19, '守株待兔', '韩非子·五蠹', 3, NULL, '死守经验，不知变通', '主动进取，不守株待兔', '警示', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (20, '掩耳盗铃', '吕氏春秋·自知', 3, NULL, '自欺欺人', '正视现实，不自欺欺人', '警示', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (21, '邯郸学步', '庄子·秋水', 3, NULL, '盲目模仿，失去自我', '保持本色，不盲目模仿', '警示', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (22, '买椟还珠', '韩非子·外储说左上', 3, NULL, '取舍不当，主次不分', '抓住重点，不因小失大', '警示', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (23, '狐假虎威', '战国策·楚策一', 3, NULL, '倚仗他人威势欺压人', '依靠自己，不仗势欺人', '警示', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (24, '杯弓蛇影', '晋书·乐广传', 7, NULL, '疑神疑鬼，自相惊扰', '消除疑虑，不杯弓蛇影', '警示', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (25, '对牛弹琴', '弘明集', 8, NULL, '说话不看对象', '说话要看对象，讲究方法', '警示', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (26, '朝三暮四', '庄子·齐物论', 3, NULL, '反复无常，变化多端', '坚守本心，不朝三暮四', '警示', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (27, '井底之蛙', '庄子·秋水', 3, NULL, '见识短浅', '开阔视野，不做井底之蛙', '警示', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (28, '叶公好龙', '新序·杂事', 5, NULL, '表面爱好，实际畏惧', '言行一致，不叶公好龙', '警示', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (29, '愚公移山', '列子·汤问', 3, NULL, '坚持不懈，改造自然', '持之以恒，终能成功', '励志', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');
INSERT INTO `allusion` VALUES (30, '精卫填海', '山海经·北山经', 3, NULL, '意志坚定，不畏艰难', '坚持不懈，终有回报', '励志', NULL, '2026-04-09 07:45:12', '2026-04-09 07:45:12');

-- ----------------------------
-- Table structure for dynasty
-- ----------------------------
DROP TABLE IF EXISTS `dynasty`;
CREATE TABLE `dynasty`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '朝代ID，主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '朝代名称',
  `start_year` int NOT NULL COMMENT '起始年份（公元前为负值）',
  `end_year` int NOT NULL COMMENT '结束年份（公元前为负值）',
  `capital` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '都城',
  `founder` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '开国者/建立者',
  `map_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '朝代地图路径',
  `dynasty_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '朝代简介描述',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dynasty_name`(`name` ASC) USING BTREE COMMENT '按朝代名称索引'
) ENGINE = InnoDB AUTO_INCREMENT = 85 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '存储朝代基础信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dynasty
-- ----------------------------
INSERT INTO `dynasty` VALUES (1, '夏', -2070, -1600, '阳城', '禹', '/maps/xia.jpg', '中国历史上第一个世袭制王朝，开启华夏文明', '2026-04-09 07:42:08', '2026-04-09 07:42:08');
INSERT INTO `dynasty` VALUES (2, '商', -1600, -1046, '殷', '汤', '/maps/shang.jpg', '中国第一个有直接文字记载的王朝，青铜文明鼎盛', '2026-04-09 07:42:08', '2026-04-09 07:42:08');
INSERT INTO `dynasty` VALUES (3, '周', -1046, -256, '镐京', '周武王', '/maps/xizhou.jpg', '中国历史上最长的王朝，分西周和东周', '2026-04-09 07:42:08', '2026-04-09 07:42:08');
INSERT INTO `dynasty` VALUES (4, '秦', -221, -207, '咸阳', '秦始皇', '/maps/qin.jpg', '中国历史上第一个大一统王朝', '2026-04-09 07:42:08', '2026-04-09 07:42:08');
INSERT INTO `dynasty` VALUES (5, '汉', -202, 220, '长安', '汉高祖', '/maps/xihan.jpg', '中国历史上强盛的王朝之一，分西汉和东汉', '2026-04-09 07:42:08', '2026-04-09 07:42:08');
INSERT INTO `dynasty` VALUES (6, '三国', 220, 280, '洛阳/成都/建业', '曹丕/刘备/孙权', '/maps/sanguo.jpg', '魏、蜀、吴三国鼎立时期', '2026-04-09 07:42:08', '2026-04-09 07:42:08');
INSERT INTO `dynasty` VALUES (7, '晋', 265, 420, '洛阳', '司马炎', '/maps/xijin.jpg', '分为西晋和东晋，短暂统一后陷入分裂', '2026-04-09 07:42:08', '2026-04-09 07:42:08');
INSERT INTO `dynasty` VALUES (8, '南北朝', 420, 589, '多个都城', '多个政权', '/maps/nanbeichao.jpg', '南朝与北朝并立的时期，民族大融合', '2026-04-09 07:42:08', '2026-04-09 07:42:08');
INSERT INTO `dynasty` VALUES (9, '隋', 581, 618, '大兴', '杨坚', '/maps/sui.jpg', '开创科举制度和大运河，结束三百年分裂', '2026-04-09 07:42:08', '2026-04-09 07:42:08');
INSERT INTO `dynasty` VALUES (10, '唐', 618, 907, '长安', '李渊', '/maps/tang.jpg', '中国历史上最强盛的朝代之一', '2026-04-09 07:42:08', '2026-04-09 07:42:08');
INSERT INTO `dynasty` VALUES (11, '宋', 960, 1279, '东京', '赵匡胤', '/maps/liaobeisong.jpg', '经济文化繁荣的时期，分北宋和南宋', '2026-04-09 07:42:08', '2026-04-09 07:42:08');
INSERT INTO `dynasty` VALUES (12, '元', 1271, 1368, '大都', '忽必烈', '/maps/yuan.jpg', '中国历史上首个由少数民族建立的大一统王朝', '2026-04-09 07:42:08', '2026-04-09 07:42:08');
INSERT INTO `dynasty` VALUES (13, '明', 1368, 1644, '南京', '朱元璋', '/maps/ming.jpg', '中国历史上最后一个汉族建立的王朝', '2026-04-09 07:42:08', '2026-04-09 07:42:08');
INSERT INTO `dynasty` VALUES (14, '清', 1636, 1912, '北京', '皇太极', '/maps/qing.jpg', '中国历史上最后一个封建王朝', '2026-04-09 07:42:08', '2026-04-09 07:42:08');

-- ----------------------------
-- Table structure for dynasty_stage
-- ----------------------------
DROP TABLE IF EXISTS `dynasty_stage`;
CREATE TABLE `dynasty_stage`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '阶段ID，主键',
  `dynasty_id` bigint NOT NULL COMMENT '所属朝代ID',
  `stage_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '阶段名称（如：盛唐、晚唐）',
  `start_year` int NOT NULL COMMENT '阶段起始年份',
  `end_year` int NOT NULL COMMENT '阶段结束年份',
  `stage_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '阶段详细描述',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dynasty_stage_dynasty_id`(`dynasty_id` ASC) USING BTREE COMMENT '按所属朝代ID索引',
  CONSTRAINT `dynasty_stage_ibfk_1` FOREIGN KEY (`dynasty_id`) REFERENCES `dynasty` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 381 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '存储长跨度朝代细分阶段表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dynasty_stage
-- ----------------------------
INSERT INTO `dynasty_stage` VALUES (1, 1, '早夏', -2070, -1900, '夏朝早期阶段', '2026-04-02 21:09:47', '2026-04-02 21:09:47');
INSERT INTO `dynasty_stage` VALUES (2, 1, '晚夏', -1899, -1600, '夏朝晚期阶段', '2026-04-02 21:09:47', '2026-04-02 21:09:47');
INSERT INTO `dynasty_stage` VALUES (3, 2, '早商', -1600, -1400, '商朝早期', '2026-04-02 21:09:47', '2026-04-02 21:09:47');
INSERT INTO `dynasty_stage` VALUES (4, 2, '晚商', -1399, -1046, '商朝晚期', '2026-04-02 21:09:47', '2026-04-02 21:09:47');
INSERT INTO `dynasty_stage` VALUES (5, 3, '西周', -1046, -771, '礼乐文明高峰', '2026-04-02 21:09:47', '2026-04-02 21:09:47');
INSERT INTO `dynasty_stage` VALUES (6, 3, '东周', -770, -256, '春秋战国时期', '2026-04-02 21:09:47', '2026-04-02 21:09:47');
INSERT INTO `dynasty_stage` VALUES (7, 4, '秦统一前', -221, -214, '秦始皇统一六国前', '2026-04-02 21:09:47', '2026-04-02 21:09:47');
INSERT INTO `dynasty_stage` VALUES (8, 4, '秦统一后', -213, -207, '秦朝统一后', '2026-04-02 21:09:47', '2026-04-02 21:09:47');
INSERT INTO `dynasty_stage` VALUES (9, 5, '西汉', -202, 9, '汉朝前期', '2026-04-02 21:09:47', '2026-04-02 21:09:47');
INSERT INTO `dynasty_stage` VALUES (10, 5, '东汉', 25, 220, '汉朝后期', '2026-04-02 21:09:47', '2026-04-02 21:09:47');
INSERT INTO `dynasty_stage` VALUES (11, 6, '曹魏前期', 220, 240, '曹魏政权初期', '2026-04-02 21:09:47', '2026-04-02 21:09:47');
INSERT INTO `dynasty_stage` VALUES (12, 6, '曹魏后期', 241, 265, '曹魏政权后期', '2026-04-02 21:09:47', '2026-04-02 21:09:47');
INSERT INTO `dynasty_stage` VALUES (17, 9, '盛唐', 618, 755, '唐朝最繁荣时期', '2026-04-02 21:09:47', '2026-04-02 21:09:47');
INSERT INTO `dynasty_stage` VALUES (18, 9, '晚唐', 756, 907, '唐朝衰落时期', '2026-04-02 21:09:47', '2026-04-02 21:09:47');
INSERT INTO `dynasty_stage` VALUES (19, 10, '北宋', 960, 1127, '宋朝前期', '2026-04-02 21:09:47', '2026-04-02 21:09:47');
INSERT INTO `dynasty_stage` VALUES (20, 10, '南宋', 1128, 1279, '宋朝后期', '2026-04-02 21:09:47', '2026-04-02 21:09:47');
INSERT INTO `dynasty_stage` VALUES (21, 1, '早夏', -2070, -1900, '夏朝早期阶段', '2026-04-02 22:06:42', '2026-04-02 22:06:42');
INSERT INTO `dynasty_stage` VALUES (22, 1, '晚夏', -1899, -1600, '夏朝晚期阶段', '2026-04-02 22:06:42', '2026-04-02 22:06:42');
INSERT INTO `dynasty_stage` VALUES (23, 2, '早商', -1600, -1400, '商朝早期', '2026-04-02 22:06:42', '2026-04-02 22:06:42');
INSERT INTO `dynasty_stage` VALUES (24, 2, '晚商', -1399, -1046, '商朝晚期', '2026-04-02 22:06:42', '2026-04-02 22:06:42');
INSERT INTO `dynasty_stage` VALUES (25, 3, '西周', -1046, -771, '礼乐文明高峰', '2026-04-02 22:06:42', '2026-04-02 22:06:42');
INSERT INTO `dynasty_stage` VALUES (26, 3, '东周', -770, -256, '春秋战国时期', '2026-04-02 22:06:42', '2026-04-02 22:06:42');
INSERT INTO `dynasty_stage` VALUES (27, 4, '秦统一前', -221, -214, '秦始皇统一六国前', '2026-04-02 22:06:42', '2026-04-02 22:06:42');
INSERT INTO `dynasty_stage` VALUES (28, 4, '秦统一后', -213, -207, '秦朝统一后', '2026-04-02 22:06:42', '2026-04-02 22:06:42');
INSERT INTO `dynasty_stage` VALUES (29, 5, '西汉', -202, 9, '汉朝前期', '2026-04-02 22:06:42', '2026-04-02 22:06:42');
INSERT INTO `dynasty_stage` VALUES (30, 5, '东汉', 25, 220, '汉朝后期', '2026-04-02 22:06:42', '2026-04-02 22:06:42');
INSERT INTO `dynasty_stage` VALUES (31, 6, '曹魏前期', 220, 240, '曹魏政权初期', '2026-04-02 22:06:42', '2026-04-02 22:06:42');
INSERT INTO `dynasty_stage` VALUES (32, 6, '曹魏后期', 241, 265, '曹魏政权后期', '2026-04-02 22:06:42', '2026-04-02 22:06:42');
INSERT INTO `dynasty_stage` VALUES (37, 9, '盛唐', 618, 755, '唐朝最繁荣时期', '2026-04-02 22:06:42', '2026-04-02 22:06:42');
INSERT INTO `dynasty_stage` VALUES (38, 9, '晚唐', 756, 907, '唐朝衰落时期', '2026-04-02 22:06:42', '2026-04-02 22:06:42');
INSERT INTO `dynasty_stage` VALUES (39, 10, '北宋', 960, 1127, '宋朝前期', '2026-04-02 22:06:42', '2026-04-02 22:06:42');
INSERT INTO `dynasty_stage` VALUES (40, 10, '南宋', 1128, 1279, '宋朝后期', '2026-04-02 22:06:42', '2026-04-02 22:06:42');
INSERT INTO `dynasty_stage` VALUES (41, 1, '早夏', -2070, -1900, '夏朝早期阶段', '2026-04-03 07:10:07', '2026-04-03 07:10:07');
INSERT INTO `dynasty_stage` VALUES (42, 1, '晚夏', -1899, -1600, '夏朝晚期阶段', '2026-04-03 07:10:07', '2026-04-03 07:10:07');
INSERT INTO `dynasty_stage` VALUES (43, 2, '早商', -1600, -1400, '商朝早期', '2026-04-03 07:10:07', '2026-04-03 07:10:07');
INSERT INTO `dynasty_stage` VALUES (44, 2, '晚商', -1399, -1046, '商朝晚期', '2026-04-03 07:10:07', '2026-04-03 07:10:07');
INSERT INTO `dynasty_stage` VALUES (45, 3, '西周', -1046, -771, '礼乐文明高峰', '2026-04-03 07:10:07', '2026-04-03 07:10:07');
INSERT INTO `dynasty_stage` VALUES (46, 3, '东周', -770, -256, '春秋战国时期', '2026-04-03 07:10:07', '2026-04-03 07:10:07');
INSERT INTO `dynasty_stage` VALUES (47, 4, '秦统一前', -221, -214, '秦始皇统一六国前', '2026-04-03 07:10:07', '2026-04-03 07:10:07');
INSERT INTO `dynasty_stage` VALUES (48, 4, '秦统一后', -213, -207, '秦朝统一后', '2026-04-03 07:10:07', '2026-04-03 07:10:07');
INSERT INTO `dynasty_stage` VALUES (49, 5, '西汉', -202, 9, '汉朝前期', '2026-04-03 07:10:07', '2026-04-03 07:10:07');
INSERT INTO `dynasty_stage` VALUES (50, 5, '东汉', 25, 220, '汉朝后期', '2026-04-03 07:10:07', '2026-04-03 07:10:07');
INSERT INTO `dynasty_stage` VALUES (51, 6, '曹魏前期', 220, 240, '曹魏政权初期', '2026-04-03 07:10:07', '2026-04-03 07:10:07');
INSERT INTO `dynasty_stage` VALUES (52, 6, '曹魏后期', 241, 265, '曹魏政权后期', '2026-04-03 07:10:07', '2026-04-03 07:10:07');
INSERT INTO `dynasty_stage` VALUES (57, 9, '盛唐', 618, 755, '唐朝最繁荣时期', '2026-04-03 07:10:07', '2026-04-03 07:10:07');
INSERT INTO `dynasty_stage` VALUES (58, 9, '晚唐', 756, 907, '唐朝衰落时期', '2026-04-03 07:10:07', '2026-04-03 07:10:07');
INSERT INTO `dynasty_stage` VALUES (59, 10, '北宋', 960, 1127, '宋朝前期', '2026-04-03 07:10:07', '2026-04-03 07:10:07');
INSERT INTO `dynasty_stage` VALUES (60, 10, '南宋', 1128, 1279, '宋朝后期', '2026-04-03 07:10:07', '2026-04-03 07:10:07');
INSERT INTO `dynasty_stage` VALUES (61, 1, '早夏', -2070, -1900, '夏朝早期阶段', '2026-04-06 11:57:03', '2026-04-06 11:57:03');
INSERT INTO `dynasty_stage` VALUES (62, 1, '晚夏', -1899, -1600, '夏朝晚期阶段', '2026-04-06 11:57:03', '2026-04-06 11:57:03');
INSERT INTO `dynasty_stage` VALUES (63, 2, '早商', -1600, -1400, '商朝早期', '2026-04-06 11:57:03', '2026-04-06 11:57:03');
INSERT INTO `dynasty_stage` VALUES (64, 2, '晚商', -1399, -1046, '商朝晚期', '2026-04-06 11:57:03', '2026-04-06 11:57:03');
INSERT INTO `dynasty_stage` VALUES (65, 3, '西周', -1046, -771, '礼乐文明高峰', '2026-04-06 11:57:03', '2026-04-06 11:57:03');
INSERT INTO `dynasty_stage` VALUES (66, 3, '东周', -770, -256, '春秋战国时期', '2026-04-06 11:57:03', '2026-04-06 11:57:03');
INSERT INTO `dynasty_stage` VALUES (67, 4, '秦统一前', -221, -214, '秦始皇统一六国前', '2026-04-06 11:57:03', '2026-04-06 11:57:03');
INSERT INTO `dynasty_stage` VALUES (68, 4, '秦统一后', -213, -207, '秦朝统一后', '2026-04-06 11:57:03', '2026-04-06 11:57:03');
INSERT INTO `dynasty_stage` VALUES (69, 5, '西汉', -202, 9, '汉朝前期', '2026-04-06 11:57:03', '2026-04-06 11:57:03');
INSERT INTO `dynasty_stage` VALUES (70, 5, '东汉', 25, 220, '汉朝后期', '2026-04-06 11:57:03', '2026-04-06 11:57:03');
INSERT INTO `dynasty_stage` VALUES (71, 6, '曹魏前期', 220, 240, '曹魏政权初期', '2026-04-06 11:57:03', '2026-04-06 11:57:03');
INSERT INTO `dynasty_stage` VALUES (72, 6, '曹魏后期', 241, 265, '曹魏政权后期', '2026-04-06 11:57:03', '2026-04-06 11:57:03');
INSERT INTO `dynasty_stage` VALUES (77, 9, '盛唐', 618, 755, '唐朝最繁荣时期', '2026-04-06 11:57:03', '2026-04-06 11:57:03');
INSERT INTO `dynasty_stage` VALUES (78, 9, '晚唐', 756, 907, '唐朝衰落时期', '2026-04-06 11:57:03', '2026-04-06 11:57:03');
INSERT INTO `dynasty_stage` VALUES (79, 10, '北宋', 960, 1127, '宋朝前期', '2026-04-06 11:57:03', '2026-04-06 11:57:03');
INSERT INTO `dynasty_stage` VALUES (80, 10, '南宋', 1128, 1279, '宋朝后期', '2026-04-06 11:57:03', '2026-04-06 11:57:03');
INSERT INTO `dynasty_stage` VALUES (81, 1, '早夏', -2070, -1900, '夏朝早期阶段', '2026-04-06 12:18:37', '2026-04-06 12:18:37');
INSERT INTO `dynasty_stage` VALUES (82, 1, '晚夏', -1899, -1600, '夏朝晚期阶段', '2026-04-06 12:18:37', '2026-04-06 12:18:37');
INSERT INTO `dynasty_stage` VALUES (83, 2, '早商', -1600, -1400, '商朝早期', '2026-04-06 12:18:37', '2026-04-06 12:18:37');
INSERT INTO `dynasty_stage` VALUES (84, 2, '晚商', -1399, -1046, '商朝晚期', '2026-04-06 12:18:37', '2026-04-06 12:18:37');
INSERT INTO `dynasty_stage` VALUES (85, 3, '西周', -1046, -771, '礼乐文明高峰', '2026-04-06 12:18:37', '2026-04-06 12:18:37');
INSERT INTO `dynasty_stage` VALUES (86, 3, '东周', -770, -256, '春秋战国时期', '2026-04-06 12:18:37', '2026-04-06 12:18:37');
INSERT INTO `dynasty_stage` VALUES (87, 4, '秦统一前', -221, -214, '秦始皇统一六国前', '2026-04-06 12:18:37', '2026-04-06 12:18:37');
INSERT INTO `dynasty_stage` VALUES (88, 4, '秦统一后', -213, -207, '秦朝统一后', '2026-04-06 12:18:37', '2026-04-06 12:18:37');
INSERT INTO `dynasty_stage` VALUES (89, 5, '西汉', -202, 9, '汉朝前期', '2026-04-06 12:18:37', '2026-04-06 12:18:37');
INSERT INTO `dynasty_stage` VALUES (90, 5, '东汉', 25, 220, '汉朝后期', '2026-04-06 12:18:37', '2026-04-06 12:18:37');
INSERT INTO `dynasty_stage` VALUES (91, 6, '曹魏前期', 220, 240, '曹魏政权初期', '2026-04-06 12:18:37', '2026-04-06 12:18:37');
INSERT INTO `dynasty_stage` VALUES (92, 6, '曹魏后期', 241, 265, '曹魏政权后期', '2026-04-06 12:18:37', '2026-04-06 12:18:37');
INSERT INTO `dynasty_stage` VALUES (97, 9, '盛唐', 618, 755, '唐朝最繁荣时期', '2026-04-06 12:18:37', '2026-04-06 12:18:37');
INSERT INTO `dynasty_stage` VALUES (98, 9, '晚唐', 756, 907, '唐朝衰落时期', '2026-04-06 12:18:37', '2026-04-06 12:18:37');
INSERT INTO `dynasty_stage` VALUES (99, 10, '北宋', 960, 1127, '宋朝前期', '2026-04-06 12:18:37', '2026-04-06 12:18:37');
INSERT INTO `dynasty_stage` VALUES (100, 10, '南宋', 1128, 1279, '宋朝后期', '2026-04-06 12:18:37', '2026-04-06 12:18:37');
INSERT INTO `dynasty_stage` VALUES (101, 1, '早夏', -2070, -1900, '夏朝早期阶段', '2026-04-06 12:33:47', '2026-04-06 12:33:47');
INSERT INTO `dynasty_stage` VALUES (102, 1, '晚夏', -1899, -1600, '夏朝晚期阶段', '2026-04-06 12:33:47', '2026-04-06 12:33:47');
INSERT INTO `dynasty_stage` VALUES (103, 2, '早商', -1600, -1400, '商朝早期', '2026-04-06 12:33:47', '2026-04-06 12:33:47');
INSERT INTO `dynasty_stage` VALUES (104, 2, '晚商', -1399, -1046, '商朝晚期', '2026-04-06 12:33:47', '2026-04-06 12:33:47');
INSERT INTO `dynasty_stage` VALUES (105, 3, '西周', -1046, -771, '礼乐文明高峰', '2026-04-06 12:33:47', '2026-04-06 12:33:47');
INSERT INTO `dynasty_stage` VALUES (106, 3, '东周', -770, -256, '春秋战国时期', '2026-04-06 12:33:47', '2026-04-06 12:33:47');
INSERT INTO `dynasty_stage` VALUES (107, 4, '秦统一前', -221, -214, '秦始皇统一六国前', '2026-04-06 12:33:47', '2026-04-06 12:33:47');
INSERT INTO `dynasty_stage` VALUES (108, 4, '秦统一后', -213, -207, '秦朝统一后', '2026-04-06 12:33:47', '2026-04-06 12:33:47');
INSERT INTO `dynasty_stage` VALUES (109, 5, '西汉', -202, 9, '汉朝前期', '2026-04-06 12:33:47', '2026-04-06 12:33:47');
INSERT INTO `dynasty_stage` VALUES (110, 5, '东汉', 25, 220, '汉朝后期', '2026-04-06 12:33:47', '2026-04-06 12:33:47');
INSERT INTO `dynasty_stage` VALUES (111, 6, '曹魏前期', 220, 240, '曹魏政权初期', '2026-04-06 12:33:47', '2026-04-06 12:33:47');
INSERT INTO `dynasty_stage` VALUES (112, 6, '曹魏后期', 241, 265, '曹魏政权后期', '2026-04-06 12:33:47', '2026-04-06 12:33:47');
INSERT INTO `dynasty_stage` VALUES (117, 9, '盛唐', 618, 755, '唐朝最繁荣时期', '2026-04-06 12:33:47', '2026-04-06 12:33:47');
INSERT INTO `dynasty_stage` VALUES (118, 9, '晚唐', 756, 907, '唐朝衰落时期', '2026-04-06 12:33:47', '2026-04-06 12:33:47');
INSERT INTO `dynasty_stage` VALUES (119, 10, '北宋', 960, 1127, '宋朝前期', '2026-04-06 12:33:47', '2026-04-06 12:33:47');
INSERT INTO `dynasty_stage` VALUES (120, 10, '南宋', 1128, 1279, '宋朝后期', '2026-04-06 12:33:47', '2026-04-06 12:33:47');
INSERT INTO `dynasty_stage` VALUES (121, 1, '早夏', -2070, -1900, '夏朝早期阶段', '2026-04-06 13:14:55', '2026-04-06 13:14:55');
INSERT INTO `dynasty_stage` VALUES (122, 1, '晚夏', -1899, -1600, '夏朝晚期阶段', '2026-04-06 13:14:55', '2026-04-06 13:14:55');
INSERT INTO `dynasty_stage` VALUES (123, 2, '早商', -1600, -1400, '商朝早期', '2026-04-06 13:14:55', '2026-04-06 13:14:55');
INSERT INTO `dynasty_stage` VALUES (124, 2, '晚商', -1399, -1046, '商朝晚期', '2026-04-06 13:14:55', '2026-04-06 13:14:55');
INSERT INTO `dynasty_stage` VALUES (125, 3, '西周', -1046, -771, '礼乐文明高峰', '2026-04-06 13:14:55', '2026-04-06 13:14:55');
INSERT INTO `dynasty_stage` VALUES (126, 3, '东周', -770, -256, '春秋战国时期', '2026-04-06 13:14:55', '2026-04-06 13:14:55');
INSERT INTO `dynasty_stage` VALUES (127, 4, '秦统一前', -221, -214, '秦始皇统一六国前', '2026-04-06 13:14:55', '2026-04-06 13:14:55');
INSERT INTO `dynasty_stage` VALUES (128, 4, '秦统一后', -213, -207, '秦朝统一后', '2026-04-06 13:14:55', '2026-04-06 13:14:55');
INSERT INTO `dynasty_stage` VALUES (129, 5, '西汉', -202, 9, '汉朝前期', '2026-04-06 13:14:55', '2026-04-06 13:14:55');
INSERT INTO `dynasty_stage` VALUES (130, 5, '东汉', 25, 220, '汉朝后期', '2026-04-06 13:14:55', '2026-04-06 13:14:55');
INSERT INTO `dynasty_stage` VALUES (131, 6, '曹魏前期', 220, 240, '曹魏政权初期', '2026-04-06 13:14:55', '2026-04-06 13:14:55');
INSERT INTO `dynasty_stage` VALUES (132, 6, '曹魏后期', 241, 265, '曹魏政权后期', '2026-04-06 13:14:55', '2026-04-06 13:14:55');
INSERT INTO `dynasty_stage` VALUES (137, 9, '盛唐', 618, 755, '唐朝最繁荣时期', '2026-04-06 13:14:55', '2026-04-06 13:14:55');
INSERT INTO `dynasty_stage` VALUES (138, 9, '晚唐', 756, 907, '唐朝衰落时期', '2026-04-06 13:14:55', '2026-04-06 13:14:55');
INSERT INTO `dynasty_stage` VALUES (139, 10, '北宋', 960, 1127, '宋朝前期', '2026-04-06 13:14:55', '2026-04-06 13:14:55');
INSERT INTO `dynasty_stage` VALUES (140, 10, '南宋', 1128, 1279, '宋朝后期', '2026-04-06 13:14:55', '2026-04-06 13:14:55');
INSERT INTO `dynasty_stage` VALUES (141, 1, '早夏', -2070, -1900, '夏朝早期阶段', '2026-04-06 13:23:23', '2026-04-06 13:23:23');
INSERT INTO `dynasty_stage` VALUES (142, 1, '晚夏', -1899, -1600, '夏朝晚期阶段', '2026-04-06 13:23:23', '2026-04-06 13:23:23');
INSERT INTO `dynasty_stage` VALUES (143, 2, '早商', -1600, -1400, '商朝早期', '2026-04-06 13:23:23', '2026-04-06 13:23:23');
INSERT INTO `dynasty_stage` VALUES (144, 2, '晚商', -1399, -1046, '商朝晚期', '2026-04-06 13:23:23', '2026-04-06 13:23:23');
INSERT INTO `dynasty_stage` VALUES (145, 3, '西周', -1046, -771, '礼乐文明高峰', '2026-04-06 13:23:23', '2026-04-06 13:23:23');
INSERT INTO `dynasty_stage` VALUES (146, 3, '东周', -770, -256, '春秋战国时期', '2026-04-06 13:23:23', '2026-04-06 13:23:23');
INSERT INTO `dynasty_stage` VALUES (147, 4, '秦统一前', -221, -214, '秦始皇统一六国前', '2026-04-06 13:23:23', '2026-04-06 13:23:23');
INSERT INTO `dynasty_stage` VALUES (148, 4, '秦统一后', -213, -207, '秦朝统一后', '2026-04-06 13:23:23', '2026-04-06 13:23:23');
INSERT INTO `dynasty_stage` VALUES (149, 5, '西汉', -202, 9, '汉朝前期', '2026-04-06 13:23:23', '2026-04-06 13:23:23');
INSERT INTO `dynasty_stage` VALUES (150, 5, '东汉', 25, 220, '汉朝后期', '2026-04-06 13:23:23', '2026-04-06 13:23:23');
INSERT INTO `dynasty_stage` VALUES (151, 6, '曹魏前期', 220, 240, '曹魏政权初期', '2026-04-06 13:23:23', '2026-04-06 13:23:23');
INSERT INTO `dynasty_stage` VALUES (152, 6, '曹魏后期', 241, 265, '曹魏政权后期', '2026-04-06 13:23:23', '2026-04-06 13:23:23');
INSERT INTO `dynasty_stage` VALUES (157, 9, '盛唐', 618, 755, '唐朝最繁荣时期', '2026-04-06 13:23:23', '2026-04-06 13:23:23');
INSERT INTO `dynasty_stage` VALUES (158, 9, '晚唐', 756, 907, '唐朝衰落时期', '2026-04-06 13:23:23', '2026-04-06 13:23:23');
INSERT INTO `dynasty_stage` VALUES (159, 10, '北宋', 960, 1127, '宋朝前期', '2026-04-06 13:23:23', '2026-04-06 13:23:23');
INSERT INTO `dynasty_stage` VALUES (160, 10, '南宋', 1128, 1279, '宋朝后期', '2026-04-06 13:23:23', '2026-04-06 13:23:23');
INSERT INTO `dynasty_stage` VALUES (161, 1, '早夏', -2070, -1900, '夏朝早期阶段', '2026-04-06 13:33:41', '2026-04-06 13:33:41');
INSERT INTO `dynasty_stage` VALUES (162, 1, '晚夏', -1899, -1600, '夏朝晚期阶段', '2026-04-06 13:33:41', '2026-04-06 13:33:41');
INSERT INTO `dynasty_stage` VALUES (163, 2, '早商', -1600, -1400, '商朝早期', '2026-04-06 13:33:41', '2026-04-06 13:33:41');
INSERT INTO `dynasty_stage` VALUES (164, 2, '晚商', -1399, -1046, '商朝晚期', '2026-04-06 13:33:41', '2026-04-06 13:33:41');
INSERT INTO `dynasty_stage` VALUES (165, 3, '西周', -1046, -771, '礼乐文明高峰', '2026-04-06 13:33:41', '2026-04-06 13:33:41');
INSERT INTO `dynasty_stage` VALUES (166, 3, '东周', -770, -256, '春秋战国时期', '2026-04-06 13:33:41', '2026-04-06 13:33:41');
INSERT INTO `dynasty_stage` VALUES (167, 4, '秦统一前', -221, -214, '秦始皇统一六国前', '2026-04-06 13:33:41', '2026-04-06 13:33:41');
INSERT INTO `dynasty_stage` VALUES (168, 4, '秦统一后', -213, -207, '秦朝统一后', '2026-04-06 13:33:41', '2026-04-06 13:33:41');
INSERT INTO `dynasty_stage` VALUES (169, 5, '西汉', -202, 9, '汉朝前期', '2026-04-06 13:33:41', '2026-04-06 13:33:41');
INSERT INTO `dynasty_stage` VALUES (170, 5, '东汉', 25, 220, '汉朝后期', '2026-04-06 13:33:41', '2026-04-06 13:33:41');
INSERT INTO `dynasty_stage` VALUES (171, 6, '曹魏前期', 220, 240, '曹魏政权初期', '2026-04-06 13:33:41', '2026-04-06 13:33:41');
INSERT INTO `dynasty_stage` VALUES (172, 6, '曹魏后期', 241, 265, '曹魏政权后期', '2026-04-06 13:33:41', '2026-04-06 13:33:41');
INSERT INTO `dynasty_stage` VALUES (177, 9, '盛唐', 618, 755, '唐朝最繁荣时期', '2026-04-06 13:33:41', '2026-04-06 13:33:41');
INSERT INTO `dynasty_stage` VALUES (178, 9, '晚唐', 756, 907, '唐朝衰落时期', '2026-04-06 13:33:41', '2026-04-06 13:33:41');
INSERT INTO `dynasty_stage` VALUES (179, 10, '北宋', 960, 1127, '宋朝前期', '2026-04-06 13:33:41', '2026-04-06 13:33:41');
INSERT INTO `dynasty_stage` VALUES (180, 10, '南宋', 1128, 1279, '宋朝后期', '2026-04-06 13:33:41', '2026-04-06 13:33:41');
INSERT INTO `dynasty_stage` VALUES (181, 1, '早夏', -2070, -1900, '夏朝早期阶段', '2026-04-06 13:58:53', '2026-04-06 13:58:53');
INSERT INTO `dynasty_stage` VALUES (182, 1, '晚夏', -1899, -1600, '夏朝晚期阶段', '2026-04-06 13:58:53', '2026-04-06 13:58:53');
INSERT INTO `dynasty_stage` VALUES (183, 2, '早商', -1600, -1400, '商朝早期', '2026-04-06 13:58:53', '2026-04-06 13:58:53');
INSERT INTO `dynasty_stage` VALUES (184, 2, '晚商', -1399, -1046, '商朝晚期', '2026-04-06 13:58:53', '2026-04-06 13:58:53');
INSERT INTO `dynasty_stage` VALUES (185, 3, '西周', -1046, -771, '礼乐文明高峰', '2026-04-06 13:58:53', '2026-04-06 13:58:53');
INSERT INTO `dynasty_stage` VALUES (186, 3, '东周', -770, -256, '春秋战国时期', '2026-04-06 13:58:53', '2026-04-06 13:58:53');
INSERT INTO `dynasty_stage` VALUES (187, 4, '秦统一前', -221, -214, '秦始皇统一六国前', '2026-04-06 13:58:53', '2026-04-06 13:58:53');
INSERT INTO `dynasty_stage` VALUES (188, 4, '秦统一后', -213, -207, '秦朝统一后', '2026-04-06 13:58:53', '2026-04-06 13:58:53');
INSERT INTO `dynasty_stage` VALUES (189, 5, '西汉', -202, 9, '汉朝前期', '2026-04-06 13:58:53', '2026-04-06 13:58:53');
INSERT INTO `dynasty_stage` VALUES (190, 5, '东汉', 25, 220, '汉朝后期', '2026-04-06 13:58:53', '2026-04-06 13:58:53');
INSERT INTO `dynasty_stage` VALUES (191, 6, '曹魏前期', 220, 240, '曹魏政权初期', '2026-04-06 13:58:53', '2026-04-06 13:58:53');
INSERT INTO `dynasty_stage` VALUES (192, 6, '曹魏后期', 241, 265, '曹魏政权后期', '2026-04-06 13:58:53', '2026-04-06 13:58:53');
INSERT INTO `dynasty_stage` VALUES (197, 9, '盛唐', 618, 755, '唐朝最繁荣时期', '2026-04-06 13:58:53', '2026-04-06 13:58:53');
INSERT INTO `dynasty_stage` VALUES (198, 9, '晚唐', 756, 907, '唐朝衰落时期', '2026-04-06 13:58:53', '2026-04-06 13:58:53');
INSERT INTO `dynasty_stage` VALUES (199, 10, '北宋', 960, 1127, '宋朝前期', '2026-04-06 13:58:53', '2026-04-06 13:58:53');
INSERT INTO `dynasty_stage` VALUES (200, 10, '南宋', 1128, 1279, '宋朝后期', '2026-04-06 13:58:53', '2026-04-06 13:58:53');
INSERT INTO `dynasty_stage` VALUES (201, 1, '早夏', -2070, -1900, '夏朝早期阶段', '2026-04-06 23:22:28', '2026-04-06 23:22:28');
INSERT INTO `dynasty_stage` VALUES (202, 1, '晚夏', -1899, -1600, '夏朝晚期阶段', '2026-04-06 23:22:28', '2026-04-06 23:22:28');
INSERT INTO `dynasty_stage` VALUES (203, 2, '早商', -1600, -1400, '商朝早期', '2026-04-06 23:22:28', '2026-04-06 23:22:28');
INSERT INTO `dynasty_stage` VALUES (204, 2, '晚商', -1399, -1046, '商朝晚期', '2026-04-06 23:22:28', '2026-04-06 23:22:28');
INSERT INTO `dynasty_stage` VALUES (205, 3, '西周', -1046, -771, '礼乐文明高峰', '2026-04-06 23:22:28', '2026-04-06 23:22:28');
INSERT INTO `dynasty_stage` VALUES (206, 3, '东周', -770, -256, '春秋战国时期', '2026-04-06 23:22:28', '2026-04-06 23:22:28');
INSERT INTO `dynasty_stage` VALUES (207, 4, '秦统一前', -221, -214, '秦始皇统一六国前', '2026-04-06 23:22:28', '2026-04-06 23:22:28');
INSERT INTO `dynasty_stage` VALUES (208, 4, '秦统一后', -213, -207, '秦朝统一后', '2026-04-06 23:22:28', '2026-04-06 23:22:28');
INSERT INTO `dynasty_stage` VALUES (209, 5, '西汉', -202, 9, '汉朝前期', '2026-04-06 23:22:28', '2026-04-06 23:22:28');
INSERT INTO `dynasty_stage` VALUES (210, 5, '东汉', 25, 220, '汉朝后期', '2026-04-06 23:22:28', '2026-04-06 23:22:28');
INSERT INTO `dynasty_stage` VALUES (211, 6, '曹魏前期', 220, 240, '曹魏政权初期', '2026-04-06 23:22:28', '2026-04-06 23:22:28');
INSERT INTO `dynasty_stage` VALUES (212, 6, '曹魏后期', 241, 265, '曹魏政权后期', '2026-04-06 23:22:28', '2026-04-06 23:22:28');
INSERT INTO `dynasty_stage` VALUES (217, 9, '盛唐', 618, 755, '唐朝最繁荣时期', '2026-04-06 23:22:28', '2026-04-06 23:22:28');
INSERT INTO `dynasty_stage` VALUES (218, 9, '晚唐', 756, 907, '唐朝衰落时期', '2026-04-06 23:22:28', '2026-04-06 23:22:28');
INSERT INTO `dynasty_stage` VALUES (219, 10, '北宋', 960, 1127, '宋朝前期', '2026-04-06 23:22:28', '2026-04-06 23:22:28');
INSERT INTO `dynasty_stage` VALUES (220, 10, '南宋', 1128, 1279, '宋朝后期', '2026-04-06 23:22:28', '2026-04-06 23:22:28');
INSERT INTO `dynasty_stage` VALUES (221, 1, '早夏', -2070, -1900, '夏朝早期阶段', '2026-04-08 19:05:42', '2026-04-08 19:05:42');
INSERT INTO `dynasty_stage` VALUES (222, 1, '晚夏', -1899, -1600, '夏朝晚期阶段', '2026-04-08 19:05:42', '2026-04-08 19:05:42');
INSERT INTO `dynasty_stage` VALUES (223, 2, '早商', -1600, -1400, '商朝早期', '2026-04-08 19:05:42', '2026-04-08 19:05:42');
INSERT INTO `dynasty_stage` VALUES (224, 2, '晚商', -1399, -1046, '商朝晚期', '2026-04-08 19:05:42', '2026-04-08 19:05:42');
INSERT INTO `dynasty_stage` VALUES (225, 3, '西周', -1046, -771, '礼乐文明高峰', '2026-04-08 19:05:42', '2026-04-08 19:05:42');
INSERT INTO `dynasty_stage` VALUES (226, 3, '东周', -770, -256, '春秋战国时期', '2026-04-08 19:05:42', '2026-04-08 19:05:42');
INSERT INTO `dynasty_stage` VALUES (227, 4, '秦统一前', -221, -214, '秦始皇统一六国前', '2026-04-08 19:05:42', '2026-04-08 19:05:42');
INSERT INTO `dynasty_stage` VALUES (228, 4, '秦统一后', -213, -207, '秦朝统一后', '2026-04-08 19:05:42', '2026-04-08 19:05:42');
INSERT INTO `dynasty_stage` VALUES (229, 5, '西汉', -202, 9, '汉朝前期', '2026-04-08 19:05:42', '2026-04-08 19:05:42');
INSERT INTO `dynasty_stage` VALUES (230, 5, '东汉', 25, 220, '汉朝后期', '2026-04-08 19:05:42', '2026-04-08 19:05:42');
INSERT INTO `dynasty_stage` VALUES (231, 6, '曹魏前期', 220, 240, '曹魏政权初期', '2026-04-08 19:05:42', '2026-04-08 19:05:42');
INSERT INTO `dynasty_stage` VALUES (232, 6, '曹魏后期', 241, 265, '曹魏政权后期', '2026-04-08 19:05:42', '2026-04-08 19:05:42');
INSERT INTO `dynasty_stage` VALUES (237, 9, '盛唐', 618, 755, '唐朝最繁荣时期', '2026-04-08 19:05:42', '2026-04-08 19:05:42');
INSERT INTO `dynasty_stage` VALUES (238, 9, '晚唐', 756, 907, '唐朝衰落时期', '2026-04-08 19:05:42', '2026-04-08 19:05:42');
INSERT INTO `dynasty_stage` VALUES (239, 10, '北宋', 960, 1127, '宋朝前期', '2026-04-08 19:05:42', '2026-04-08 19:05:42');
INSERT INTO `dynasty_stage` VALUES (240, 10, '南宋', 1128, 1279, '宋朝后期', '2026-04-08 19:05:42', '2026-04-08 19:05:42');
INSERT INTO `dynasty_stage` VALUES (241, 1, '早夏', -2070, -1900, '夏朝早期阶段', '2026-04-09 07:46:14', '2026-04-09 07:46:14');
INSERT INTO `dynasty_stage` VALUES (242, 1, '晚夏', -1899, -1600, '夏朝晚期阶段', '2026-04-09 07:46:14', '2026-04-09 07:46:14');
INSERT INTO `dynasty_stage` VALUES (243, 2, '早商', -1600, -1400, '商朝早期', '2026-04-09 07:46:14', '2026-04-09 07:46:14');
INSERT INTO `dynasty_stage` VALUES (244, 2, '晚商', -1399, -1046, '商朝晚期', '2026-04-09 07:46:14', '2026-04-09 07:46:14');
INSERT INTO `dynasty_stage` VALUES (245, 3, '西周', -1046, -771, '礼乐文明高峰', '2026-04-09 07:46:14', '2026-04-09 07:46:14');
INSERT INTO `dynasty_stage` VALUES (246, 3, '东周', -770, -256, '春秋战国时期', '2026-04-09 07:46:14', '2026-04-09 07:46:14');
INSERT INTO `dynasty_stage` VALUES (247, 4, '秦统一前', -221, -214, '秦始皇统一六国前', '2026-04-09 07:46:14', '2026-04-09 07:46:14');
INSERT INTO `dynasty_stage` VALUES (248, 4, '秦统一后', -213, -207, '秦朝统一后', '2026-04-09 07:46:14', '2026-04-09 07:46:14');
INSERT INTO `dynasty_stage` VALUES (249, 5, '西汉', -202, 9, '汉朝前期', '2026-04-09 07:46:14', '2026-04-09 07:46:14');
INSERT INTO `dynasty_stage` VALUES (250, 5, '东汉', 25, 220, '汉朝后期', '2026-04-09 07:46:14', '2026-04-09 07:46:14');
INSERT INTO `dynasty_stage` VALUES (251, 6, '曹魏前期', 220, 240, '曹魏政权初期', '2026-04-09 07:46:14', '2026-04-09 07:46:14');
INSERT INTO `dynasty_stage` VALUES (252, 6, '曹魏后期', 241, 265, '曹魏政权后期', '2026-04-09 07:46:14', '2026-04-09 07:46:14');
INSERT INTO `dynasty_stage` VALUES (253, 7, '蜀汉前期', 221, 240, '刘备时期', '2026-04-09 07:46:14', '2026-04-09 07:46:14');
INSERT INTO `dynasty_stage` VALUES (254, 7, '蜀汉后期', 241, 263, '诸葛亮时期', '2026-04-09 07:46:14', '2026-04-09 07:46:14');
INSERT INTO `dynasty_stage` VALUES (255, 8, '东吴前期', 222, 250, '孙权前期', '2026-04-09 07:46:14', '2026-04-09 07:46:14');
INSERT INTO `dynasty_stage` VALUES (256, 8, '东吴后期', 251, 280, '东吴后期', '2026-04-09 07:46:14', '2026-04-09 07:46:14');
INSERT INTO `dynasty_stage` VALUES (257, 9, '盛唐', 618, 755, '唐朝最繁荣时期', '2026-04-09 07:46:14', '2026-04-09 07:46:14');
INSERT INTO `dynasty_stage` VALUES (258, 9, '晚唐', 756, 907, '唐朝衰落时期', '2026-04-09 07:46:14', '2026-04-09 07:46:14');
INSERT INTO `dynasty_stage` VALUES (259, 10, '北宋', 960, 1127, '宋朝前期', '2026-04-09 07:46:14', '2026-04-09 07:46:14');
INSERT INTO `dynasty_stage` VALUES (260, 10, '南宋', 1128, 1279, '宋朝后期', '2026-04-09 07:46:14', '2026-04-09 07:46:14');
INSERT INTO `dynasty_stage` VALUES (261, 1, '早夏', -2070, -1900, '夏朝早期阶段', '2026-04-09 07:51:56', '2026-04-09 07:51:56');
INSERT INTO `dynasty_stage` VALUES (262, 1, '晚夏', -1899, -1600, '夏朝晚期阶段', '2026-04-09 07:51:56', '2026-04-09 07:51:56');
INSERT INTO `dynasty_stage` VALUES (263, 2, '早商', -1600, -1400, '商朝早期', '2026-04-09 07:51:56', '2026-04-09 07:51:56');
INSERT INTO `dynasty_stage` VALUES (264, 2, '晚商', -1399, -1046, '商朝晚期', '2026-04-09 07:51:56', '2026-04-09 07:51:56');
INSERT INTO `dynasty_stage` VALUES (265, 3, '西周', -1046, -771, '礼乐文明高峰', '2026-04-09 07:51:56', '2026-04-09 07:51:56');
INSERT INTO `dynasty_stage` VALUES (266, 3, '东周', -770, -256, '春秋战国时期', '2026-04-09 07:51:56', '2026-04-09 07:51:56');
INSERT INTO `dynasty_stage` VALUES (267, 4, '秦统一前', -221, -214, '秦始皇统一六国前', '2026-04-09 07:51:56', '2026-04-09 07:51:56');
INSERT INTO `dynasty_stage` VALUES (268, 4, '秦统一后', -213, -207, '秦朝统一后', '2026-04-09 07:51:56', '2026-04-09 07:51:56');
INSERT INTO `dynasty_stage` VALUES (269, 5, '西汉', -202, 9, '汉朝前期', '2026-04-09 07:51:56', '2026-04-09 07:51:56');
INSERT INTO `dynasty_stage` VALUES (270, 5, '东汉', 25, 220, '汉朝后期', '2026-04-09 07:51:56', '2026-04-09 07:51:56');
INSERT INTO `dynasty_stage` VALUES (271, 6, '曹魏前期', 220, 240, '曹魏政权初期', '2026-04-09 07:51:56', '2026-04-09 07:51:56');
INSERT INTO `dynasty_stage` VALUES (272, 6, '曹魏后期', 241, 265, '曹魏政权后期', '2026-04-09 07:51:56', '2026-04-09 07:51:56');
INSERT INTO `dynasty_stage` VALUES (273, 7, '蜀汉前期', 221, 240, '刘备时期', '2026-04-09 07:51:56', '2026-04-09 07:51:56');
INSERT INTO `dynasty_stage` VALUES (274, 7, '蜀汉后期', 241, 263, '诸葛亮时期', '2026-04-09 07:51:56', '2026-04-09 07:51:56');
INSERT INTO `dynasty_stage` VALUES (275, 8, '东吴前期', 222, 250, '孙权前期', '2026-04-09 07:51:56', '2026-04-09 07:51:56');
INSERT INTO `dynasty_stage` VALUES (276, 8, '东吴后期', 251, 280, '东吴后期', '2026-04-09 07:51:56', '2026-04-09 07:51:56');
INSERT INTO `dynasty_stage` VALUES (277, 9, '盛唐', 618, 755, '唐朝最繁荣时期', '2026-04-09 07:51:56', '2026-04-09 07:51:56');
INSERT INTO `dynasty_stage` VALUES (278, 9, '晚唐', 756, 907, '唐朝衰落时期', '2026-04-09 07:51:56', '2026-04-09 07:51:56');
INSERT INTO `dynasty_stage` VALUES (279, 10, '北宋', 960, 1127, '宋朝前期', '2026-04-09 07:51:56', '2026-04-09 07:51:56');
INSERT INTO `dynasty_stage` VALUES (280, 10, '南宋', 1128, 1279, '宋朝后期', '2026-04-09 07:51:56', '2026-04-09 07:51:56');
INSERT INTO `dynasty_stage` VALUES (281, 1, '早夏', -2070, -1900, '夏朝早期阶段', '2026-04-12 19:04:15', '2026-04-12 19:04:15');
INSERT INTO `dynasty_stage` VALUES (282, 1, '晚夏', -1899, -1600, '夏朝晚期阶段', '2026-04-12 19:04:15', '2026-04-12 19:04:15');
INSERT INTO `dynasty_stage` VALUES (283, 2, '早商', -1600, -1400, '商朝早期', '2026-04-12 19:04:15', '2026-04-12 19:04:15');
INSERT INTO `dynasty_stage` VALUES (284, 2, '晚商', -1399, -1046, '商朝晚期', '2026-04-12 19:04:15', '2026-04-12 19:04:15');
INSERT INTO `dynasty_stage` VALUES (285, 3, '西周', -1046, -771, '礼乐文明高峰', '2026-04-12 19:04:15', '2026-04-12 19:04:15');
INSERT INTO `dynasty_stage` VALUES (286, 3, '东周', -770, -256, '春秋战国时期', '2026-04-12 19:04:15', '2026-04-12 19:04:15');
INSERT INTO `dynasty_stage` VALUES (287, 4, '秦统一前', -221, -214, '秦始皇统一六国前', '2026-04-12 19:04:15', '2026-04-12 19:04:15');
INSERT INTO `dynasty_stage` VALUES (288, 4, '秦统一后', -213, -207, '秦朝统一后', '2026-04-12 19:04:15', '2026-04-12 19:04:15');
INSERT INTO `dynasty_stage` VALUES (289, 5, '西汉', -202, 9, '汉朝前期', '2026-04-12 19:04:15', '2026-04-12 19:04:15');
INSERT INTO `dynasty_stage` VALUES (290, 5, '东汉', 25, 220, '汉朝后期', '2026-04-12 19:04:15', '2026-04-12 19:04:15');
INSERT INTO `dynasty_stage` VALUES (291, 6, '曹魏前期', 220, 240, '曹魏政权初期', '2026-04-12 19:04:15', '2026-04-12 19:04:15');
INSERT INTO `dynasty_stage` VALUES (292, 6, '曹魏后期', 241, 265, '曹魏政权后期', '2026-04-12 19:04:15', '2026-04-12 19:04:15');
INSERT INTO `dynasty_stage` VALUES (293, 7, '蜀汉前期', 221, 240, '刘备时期', '2026-04-12 19:04:15', '2026-04-12 19:04:15');
INSERT INTO `dynasty_stage` VALUES (294, 7, '蜀汉后期', 241, 263, '诸葛亮时期', '2026-04-12 19:04:15', '2026-04-12 19:04:15');
INSERT INTO `dynasty_stage` VALUES (295, 8, '东吴前期', 222, 250, '孙权前期', '2026-04-12 19:04:15', '2026-04-12 19:04:15');
INSERT INTO `dynasty_stage` VALUES (296, 8, '东吴后期', 251, 280, '东吴后期', '2026-04-12 19:04:15', '2026-04-12 19:04:15');
INSERT INTO `dynasty_stage` VALUES (297, 9, '盛唐', 618, 755, '唐朝最繁荣时期', '2026-04-12 19:04:15', '2026-04-12 19:04:15');
INSERT INTO `dynasty_stage` VALUES (298, 9, '晚唐', 756, 907, '唐朝衰落时期', '2026-04-12 19:04:15', '2026-04-12 19:04:15');
INSERT INTO `dynasty_stage` VALUES (299, 10, '北宋', 960, 1127, '宋朝前期', '2026-04-12 19:04:15', '2026-04-12 19:04:15');
INSERT INTO `dynasty_stage` VALUES (300, 10, '南宋', 1128, 1279, '宋朝后期', '2026-04-12 19:04:15', '2026-04-12 19:04:15');
INSERT INTO `dynasty_stage` VALUES (301, 1, '早夏', -2070, -1900, '夏朝早期阶段', '2026-04-12 19:04:53', '2026-04-12 19:04:53');
INSERT INTO `dynasty_stage` VALUES (302, 1, '晚夏', -1899, -1600, '夏朝晚期阶段', '2026-04-12 19:04:53', '2026-04-12 19:04:53');
INSERT INTO `dynasty_stage` VALUES (303, 2, '早商', -1600, -1400, '商朝早期', '2026-04-12 19:04:53', '2026-04-12 19:04:53');
INSERT INTO `dynasty_stage` VALUES (304, 2, '晚商', -1399, -1046, '商朝晚期', '2026-04-12 19:04:53', '2026-04-12 19:04:53');
INSERT INTO `dynasty_stage` VALUES (305, 3, '西周', -1046, -771, '礼乐文明高峰', '2026-04-12 19:04:53', '2026-04-12 19:04:53');
INSERT INTO `dynasty_stage` VALUES (306, 3, '东周', -770, -256, '春秋战国时期', '2026-04-12 19:04:53', '2026-04-12 19:04:53');
INSERT INTO `dynasty_stage` VALUES (307, 4, '秦统一前', -221, -214, '秦始皇统一六国前', '2026-04-12 19:04:53', '2026-04-12 19:04:53');
INSERT INTO `dynasty_stage` VALUES (308, 4, '秦统一后', -213, -207, '秦朝统一后', '2026-04-12 19:04:53', '2026-04-12 19:04:53');
INSERT INTO `dynasty_stage` VALUES (309, 5, '西汉', -202, 9, '汉朝前期', '2026-04-12 19:04:53', '2026-04-12 19:04:53');
INSERT INTO `dynasty_stage` VALUES (310, 5, '东汉', 25, 220, '汉朝后期', '2026-04-12 19:04:53', '2026-04-12 19:04:53');
INSERT INTO `dynasty_stage` VALUES (311, 6, '曹魏前期', 220, 240, '曹魏政权初期', '2026-04-12 19:04:53', '2026-04-12 19:04:53');
INSERT INTO `dynasty_stage` VALUES (312, 6, '曹魏后期', 241, 265, '曹魏政权后期', '2026-04-12 19:04:53', '2026-04-12 19:04:53');
INSERT INTO `dynasty_stage` VALUES (313, 7, '蜀汉前期', 221, 240, '刘备时期', '2026-04-12 19:04:53', '2026-04-12 19:04:53');
INSERT INTO `dynasty_stage` VALUES (314, 7, '蜀汉后期', 241, 263, '诸葛亮时期', '2026-04-12 19:04:53', '2026-04-12 19:04:53');
INSERT INTO `dynasty_stage` VALUES (315, 8, '东吴前期', 222, 250, '孙权前期', '2026-04-12 19:04:53', '2026-04-12 19:04:53');
INSERT INTO `dynasty_stage` VALUES (316, 8, '东吴后期', 251, 280, '东吴后期', '2026-04-12 19:04:53', '2026-04-12 19:04:53');
INSERT INTO `dynasty_stage` VALUES (317, 9, '盛唐', 618, 755, '唐朝最繁荣时期', '2026-04-12 19:04:53', '2026-04-12 19:04:53');
INSERT INTO `dynasty_stage` VALUES (318, 9, '晚唐', 756, 907, '唐朝衰落时期', '2026-04-12 19:04:53', '2026-04-12 19:04:53');
INSERT INTO `dynasty_stage` VALUES (319, 10, '北宋', 960, 1127, '宋朝前期', '2026-04-12 19:04:53', '2026-04-12 19:04:53');
INSERT INTO `dynasty_stage` VALUES (320, 10, '南宋', 1128, 1279, '宋朝后期', '2026-04-12 19:04:53', '2026-04-12 19:04:53');
INSERT INTO `dynasty_stage` VALUES (321, 1, '早夏', -2070, -1900, '夏朝早期阶段', '2026-04-12 19:09:06', '2026-04-12 19:09:06');
INSERT INTO `dynasty_stage` VALUES (322, 1, '晚夏', -1899, -1600, '夏朝晚期阶段', '2026-04-12 19:09:06', '2026-04-12 19:09:06');
INSERT INTO `dynasty_stage` VALUES (323, 2, '早商', -1600, -1400, '商朝早期', '2026-04-12 19:09:06', '2026-04-12 19:09:06');
INSERT INTO `dynasty_stage` VALUES (324, 2, '晚商', -1399, -1046, '商朝晚期', '2026-04-12 19:09:06', '2026-04-12 19:09:06');
INSERT INTO `dynasty_stage` VALUES (325, 3, '西周', -1046, -771, '礼乐文明高峰', '2026-04-12 19:09:06', '2026-04-12 19:09:06');
INSERT INTO `dynasty_stage` VALUES (326, 3, '东周', -770, -256, '春秋战国时期', '2026-04-12 19:09:06', '2026-04-12 19:09:06');
INSERT INTO `dynasty_stage` VALUES (327, 4, '秦统一前', -221, -214, '秦始皇统一六国前', '2026-04-12 19:09:06', '2026-04-12 19:09:06');
INSERT INTO `dynasty_stage` VALUES (328, 4, '秦统一后', -213, -207, '秦朝统一后', '2026-04-12 19:09:06', '2026-04-12 19:09:06');
INSERT INTO `dynasty_stage` VALUES (329, 5, '西汉', -202, 9, '汉朝前期', '2026-04-12 19:09:06', '2026-04-12 19:09:06');
INSERT INTO `dynasty_stage` VALUES (330, 5, '东汉', 25, 220, '汉朝后期', '2026-04-12 19:09:06', '2026-04-12 19:09:06');
INSERT INTO `dynasty_stage` VALUES (331, 6, '曹魏前期', 220, 240, '曹魏政权初期', '2026-04-12 19:09:06', '2026-04-12 19:09:06');
INSERT INTO `dynasty_stage` VALUES (332, 6, '曹魏后期', 241, 265, '曹魏政权后期', '2026-04-12 19:09:06', '2026-04-12 19:09:06');
INSERT INTO `dynasty_stage` VALUES (333, 7, '蜀汉前期', 221, 240, '刘备时期', '2026-04-12 19:09:06', '2026-04-12 19:09:06');
INSERT INTO `dynasty_stage` VALUES (334, 7, '蜀汉后期', 241, 263, '诸葛亮时期', '2026-04-12 19:09:06', '2026-04-12 19:09:06');
INSERT INTO `dynasty_stage` VALUES (335, 8, '东吴前期', 222, 250, '孙权前期', '2026-04-12 19:09:06', '2026-04-12 19:09:06');
INSERT INTO `dynasty_stage` VALUES (336, 8, '东吴后期', 251, 280, '东吴后期', '2026-04-12 19:09:06', '2026-04-12 19:09:06');
INSERT INTO `dynasty_stage` VALUES (337, 9, '盛唐', 618, 755, '唐朝最繁荣时期', '2026-04-12 19:09:06', '2026-04-12 19:09:06');
INSERT INTO `dynasty_stage` VALUES (338, 9, '晚唐', 756, 907, '唐朝衰落时期', '2026-04-12 19:09:06', '2026-04-12 19:09:06');
INSERT INTO `dynasty_stage` VALUES (339, 10, '北宋', 960, 1127, '宋朝前期', '2026-04-12 19:09:06', '2026-04-12 19:09:06');
INSERT INTO `dynasty_stage` VALUES (340, 10, '南宋', 1128, 1279, '宋朝后期', '2026-04-12 19:09:06', '2026-04-12 19:09:06');
INSERT INTO `dynasty_stage` VALUES (341, 1, '早夏', -2070, -1900, '夏朝早期阶段', '2026-04-12 19:53:33', '2026-04-12 19:53:33');
INSERT INTO `dynasty_stage` VALUES (342, 1, '晚夏', -1899, -1600, '夏朝晚期阶段', '2026-04-12 19:53:33', '2026-04-12 19:53:33');
INSERT INTO `dynasty_stage` VALUES (343, 2, '早商', -1600, -1400, '商朝早期', '2026-04-12 19:53:33', '2026-04-12 19:53:33');
INSERT INTO `dynasty_stage` VALUES (344, 2, '晚商', -1399, -1046, '商朝晚期', '2026-04-12 19:53:33', '2026-04-12 19:53:33');
INSERT INTO `dynasty_stage` VALUES (345, 3, '西周', -1046, -771, '礼乐文明高峰', '2026-04-12 19:53:33', '2026-04-12 19:53:33');
INSERT INTO `dynasty_stage` VALUES (346, 3, '东周', -770, -256, '春秋战国时期', '2026-04-12 19:53:33', '2026-04-12 19:53:33');
INSERT INTO `dynasty_stage` VALUES (347, 4, '秦统一前', -221, -214, '秦始皇统一六国前', '2026-04-12 19:53:33', '2026-04-12 19:53:33');
INSERT INTO `dynasty_stage` VALUES (348, 4, '秦统一后', -213, -207, '秦朝统一后', '2026-04-12 19:53:33', '2026-04-12 19:53:33');
INSERT INTO `dynasty_stage` VALUES (349, 5, '西汉', -202, 9, '汉朝前期', '2026-04-12 19:53:33', '2026-04-12 19:53:33');
INSERT INTO `dynasty_stage` VALUES (350, 5, '东汉', 25, 220, '汉朝后期', '2026-04-12 19:53:33', '2026-04-12 19:53:33');
INSERT INTO `dynasty_stage` VALUES (351, 6, '曹魏前期', 220, 240, '曹魏政权初期', '2026-04-12 19:53:33', '2026-04-12 19:53:33');
INSERT INTO `dynasty_stage` VALUES (352, 6, '曹魏后期', 241, 265, '曹魏政权后期', '2026-04-12 19:53:33', '2026-04-12 19:53:33');
INSERT INTO `dynasty_stage` VALUES (353, 7, '蜀汉前期', 221, 240, '刘备时期', '2026-04-12 19:53:33', '2026-04-12 19:53:33');
INSERT INTO `dynasty_stage` VALUES (354, 7, '蜀汉后期', 241, 263, '诸葛亮时期', '2026-04-12 19:53:33', '2026-04-12 19:53:33');
INSERT INTO `dynasty_stage` VALUES (355, 8, '东吴前期', 222, 250, '孙权前期', '2026-04-12 19:53:33', '2026-04-12 19:53:33');
INSERT INTO `dynasty_stage` VALUES (356, 8, '东吴后期', 251, 280, '东吴后期', '2026-04-12 19:53:33', '2026-04-12 19:53:33');
INSERT INTO `dynasty_stage` VALUES (357, 9, '盛唐', 618, 755, '唐朝最繁荣时期', '2026-04-12 19:53:33', '2026-04-12 19:53:33');
INSERT INTO `dynasty_stage` VALUES (358, 9, '晚唐', 756, 907, '唐朝衰落时期', '2026-04-12 19:53:33', '2026-04-12 19:53:33');
INSERT INTO `dynasty_stage` VALUES (359, 10, '北宋', 960, 1127, '宋朝前期', '2026-04-12 19:53:33', '2026-04-12 19:53:33');
INSERT INTO `dynasty_stage` VALUES (360, 10, '南宋', 1128, 1279, '宋朝后期', '2026-04-12 19:53:33', '2026-04-12 19:53:33');
INSERT INTO `dynasty_stage` VALUES (361, 1, '早夏', -2070, -1900, '夏朝早期阶段', '2026-04-12 19:58:06', '2026-04-12 19:58:06');
INSERT INTO `dynasty_stage` VALUES (362, 1, '晚夏', -1899, -1600, '夏朝晚期阶段', '2026-04-12 19:58:06', '2026-04-12 19:58:06');
INSERT INTO `dynasty_stage` VALUES (363, 2, '早商', -1600, -1400, '商朝早期', '2026-04-12 19:58:06', '2026-04-12 19:58:06');
INSERT INTO `dynasty_stage` VALUES (364, 2, '晚商', -1399, -1046, '商朝晚期', '2026-04-12 19:58:06', '2026-04-12 19:58:06');
INSERT INTO `dynasty_stage` VALUES (365, 3, '西周', -1046, -771, '礼乐文明高峰', '2026-04-12 19:58:06', '2026-04-12 19:58:06');
INSERT INTO `dynasty_stage` VALUES (366, 3, '东周', -770, -256, '春秋战国时期', '2026-04-12 19:58:06', '2026-04-12 19:58:06');
INSERT INTO `dynasty_stage` VALUES (367, 4, '秦统一前', -221, -214, '秦始皇统一六国前', '2026-04-12 19:58:06', '2026-04-12 19:58:06');
INSERT INTO `dynasty_stage` VALUES (368, 4, '秦统一后', -213, -207, '秦朝统一后', '2026-04-12 19:58:06', '2026-04-12 19:58:06');
INSERT INTO `dynasty_stage` VALUES (369, 5, '西汉', -202, 9, '汉朝前期', '2026-04-12 19:58:06', '2026-04-12 19:58:06');
INSERT INTO `dynasty_stage` VALUES (370, 5, '东汉', 25, 220, '汉朝后期', '2026-04-12 19:58:06', '2026-04-12 19:58:06');
INSERT INTO `dynasty_stage` VALUES (371, 6, '曹魏前期', 220, 240, '曹魏政权初期', '2026-04-12 19:58:06', '2026-04-12 19:58:06');
INSERT INTO `dynasty_stage` VALUES (372, 6, '曹魏后期', 241, 265, '曹魏政权后期', '2026-04-12 19:58:06', '2026-04-12 19:58:06');
INSERT INTO `dynasty_stage` VALUES (373, 7, '蜀汉前期', 221, 240, '刘备时期', '2026-04-12 19:58:06', '2026-04-12 19:58:06');
INSERT INTO `dynasty_stage` VALUES (374, 7, '蜀汉后期', 241, 263, '诸葛亮时期', '2026-04-12 19:58:06', '2026-04-12 19:58:06');
INSERT INTO `dynasty_stage` VALUES (375, 8, '东吴前期', 222, 250, '孙权前期', '2026-04-12 19:58:06', '2026-04-12 19:58:06');
INSERT INTO `dynasty_stage` VALUES (376, 8, '东吴后期', 251, 280, '东吴后期', '2026-04-12 19:58:06', '2026-04-12 19:58:06');
INSERT INTO `dynasty_stage` VALUES (377, 9, '盛唐', 618, 755, '唐朝最繁荣时期', '2026-04-12 19:58:06', '2026-04-12 19:58:06');
INSERT INTO `dynasty_stage` VALUES (378, 9, '晚唐', 756, 907, '唐朝衰落时期', '2026-04-12 19:58:06', '2026-04-12 19:58:06');
INSERT INTO `dynasty_stage` VALUES (379, 10, '北宋', 960, 1127, '宋朝前期', '2026-04-12 19:58:06', '2026-04-12 19:58:06');
INSERT INTO `dynasty_stage` VALUES (380, 10, '南宋', 1128, 1279, '宋朝后期', '2026-04-12 19:58:06', '2026-04-12 19:58:06');

-- ----------------------------
-- Table structure for historical_event
-- ----------------------------
DROP TABLE IF EXISTS `historical_event`;
CREATE TABLE `historical_event`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '事件ID，主键',
  `dynasty_id` bigint NOT NULL COMMENT '所属朝代ID',
  `stage_id` bigint NULL DEFAULT NULL COMMENT '所属朝代阶段ID（可选）',
  `event_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件名称',
  `event_year` int NOT NULL COMMENT '事件核心年份（公元前为负值）',
  `event_bg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '事件背景描述',
  `event_process` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '事件过程描述',
  `event_impact` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '事件影响描述',
  `event_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '其他' COMMENT '事件类型（政治/军事/文化/经济/社会/其他）',
  `event_pic_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '事件配图路径',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_event_dynasty_id`(`dynasty_id` ASC) USING BTREE COMMENT '按所属朝代ID索引',
  INDEX `idx_event_stage_id`(`stage_id` ASC) USING BTREE COMMENT '按所属朝代阶段ID索引',
  INDEX `idx_event_year`(`event_year` ASC) USING BTREE COMMENT '按事件年份索引',
  INDEX `idx_event_type`(`event_type` ASC) USING BTREE COMMENT '按事件类型索引',
  CONSTRAINT `historical_event_ibfk_1` FOREIGN KEY (`dynasty_id`) REFERENCES `dynasty` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `historical_event_ibfk_2` FOREIGN KEY (`stage_id`) REFERENCES `dynasty_stage` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 42 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '存储核心历史事件信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of historical_event
-- ----------------------------
INSERT INTO `historical_event` VALUES (1, 1, NULL, '大禹治水', -2070, '洪水泛滥，民不聊生', '禹采用疏导的方法，三过家门而不入', '为夏朝建立奠定基础，开启华夏文明', 'political', '/dayuzhishui.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (2, 2, NULL, '商汤灭夏', -1600, '夏桀暴政，民怨沸腾', '商汤任用伊尹，鸣条之战大败夏军', '建立商朝，开启新的统治秩序', 'military', '/shangtangmiexia.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (3, 2, NULL, '盘庚迁殷', -1300, '商朝中期内乱不断', '盘庚将都城迁至殷，稳定局势', '商朝复兴，殷墟成为文化中心', 'political', '/pangengqianyin.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (4, 3, NULL, '牧野之战', -1046, '商纣王无道，周武王起兵', '周军与商军在牧野决战，商军倒戈', '商朝灭亡，周朝建立', 'military', '/muyezhibian.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (5, 3, NULL, '周公东征', -1042, '三监之乱威胁周朝统治', '周公旦率军东征，平定叛乱', '巩固周朝统治，分封诸侯', 'military', '/zhougongdongzheng.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (6, 3, NULL, '国人暴动', -841, '周厉王暴政，民怨沸腾', '国人起义，周厉王出逃', '召公、周公共同执政，共和行政开始', 'political', '/guorenbaodong.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (7, 4, NULL, '商鞅变法', -356, '秦国贫弱，急需富强', '商鞅推行变法，奖励耕战', '秦国强大，为统一六国奠定基础', 'political', '/shangyangbianfa.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (8, 4, NULL, '长平之战', -260, '秦赵争夺上党地区', '白起大败赵军，坑杀降卒40万', '赵国衰落，秦国统一趋势不可阻挡', 'military', '/changpingzhizhan.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (9, 4, NULL, '秦始皇统一六国', -221, '秦国实力远超六国', '先后灭韩赵魏楚燕齐六国', '中国历史上第一个大一统王朝建立', 'political', '/qinshihuangtongyi.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (10, 4, NULL, '焚书坑儒', -213, '统一思想，巩固统治', '焚毁诗书，坑杀方士儒生', '思想受到禁锢，但也统一了文化', 'political', '/fenshukengru.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (11, 5, NULL, '楚汉之争', -206, '秦朝灭亡，群雄逐鹿', '刘邦与项羽争夺天下', '刘邦获胜，建立汉朝', 'military', '/chuhanzhizheng.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (12, 5, NULL, '文景之治', -180, '汉初民生凋敝，急需休养', '文帝景帝推行黄老之术，轻徭薄赋', '国力恢复，为汉武帝盛世奠基', 'political', '/wenjingzhizhi.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (13, 5, NULL, '汉武帝改革', -140, '加强中央集权，开拓疆土', '推恩令削弱诸侯，盐铁官营', '汉朝达到鼎盛，开辟丝绸之路', 'political', '/hanwudigaige.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (14, 5, NULL, '张骞出使西域', -138, '联络大月氏夹击匈奴', '张骞两次出使西域，历经艰险', '开辟丝绸之路，促进中外交流', 'cultural', '/zhangqianchuxixiyu.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (15, 5, NULL, '司马迁著史记', -104, '继承父志，记录历史', '司马迁忍辱负重撰写史记', '中国第一部纪传体通史问世', 'cultural', '/simqianzhushiji.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (16, 6, NULL, '官渡之战', 200, '袁绍与曹操争夺北方霸权', '曹操以少胜多，火烧乌巢', '曹操统一北方，奠定曹魏基础', 'military', '/guanduzhizhan.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (17, 6, NULL, '赤壁之战', 208, '曹操南下，企图统一全国', '孙刘联军火攻曹军，大获全胜', '奠定三国鼎立格局', 'military', '/chibizhizhan.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (18, 6, NULL, '夷陵之战', 222, '刘备为关羽报仇伐吴', '陆逊火烧连营，大败蜀军', '三国鼎立格局最终形成', 'military', '/yilingzhizhan.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (19, 7, NULL, '八王之乱', 291, '晋武帝死后，宗室争权', '八位司马氏王爷相互攻杀', '西晋衰落，五胡入华', 'political', '/bawangzhiluan.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (20, 7, NULL, '淝水之战', 383, '前秦苻坚企图统一全国', '东晋谢玄以少胜多，大败前秦', '东晋得以延续，前秦瓦解', 'military', '/feishuizhizhan.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (21, 8, NULL, '孝文帝改革', 471, '北魏统一北方，需汉化稳定统治', '迁都洛阳，推行汉化政策', '促进民族融合，加速鲜卑汉化', 'political', '/xiaowendigaige.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (22, 9, NULL, '隋朝统一', 589, '南北朝分裂已久，统一趋势明显', '隋文帝杨坚灭陈，统一全国', '结束三百年分裂，重建大一统', 'political', '/suitongyitongyi.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (23, 9, NULL, '开皇之治', 581, '隋朝建立，百废待兴', '改革制度，减轻赋税', '国力强盛，为盛世奠基', 'political', '/kaihuangzhizhi.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (24, 9, NULL, '开凿大运河', 605, '加强南北联系，便于漕运', '隋炀帝征发民夫开凿运河', '促进南北经济文化交流', 'cultural', '/kaozadaiyunhe.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (25, 10, NULL, '玄武门之变', 626, '太子李建成与秦王李世民矛盾激化', '李世民在玄武门设伏，杀太子', '李世民即位，开启贞观之治', 'political', '/xuanwuwenzhibian.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (26, 10, NULL, '贞观之治', 627, '李世民即位，励精图治', '虚心纳谏，任用贤能，轻徭薄赋', '唐朝国力强盛，被誉为盛世', 'political', '/zhenguanzhizhi.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (27, 10, NULL, '安史之乱', 755, '唐玄宗后期，节度使权力过大', '安禄山、史思明发动叛乱', '唐朝由盛转衰，藩镇割据', 'military', '/anshizhiluan.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (28, 11, NULL, '陈桥兵变', 960, '后周幼主即位，政局不稳', '赵匡胤黄袍加身，建立宋朝', '宋朝建立，结束五代十国', 'political', '/chenqiaobingbian.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (29, 11, NULL, '澶渊之盟', 1004, '辽国南侵，威胁宋朝', '宋真宗亲征，与辽签订和约', '宋辽百年和平，但宋朝岁贡', 'political', '/chanyuanzhimeng.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (30, 11, NULL, '王安石变法', 1069, '宋朝积贫积弱，改革呼声高涨', '王安石推行新法，力图富国强兵', '虽遭反对，但影响深远', 'political', '/wanganshibianfa.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (31, 12, NULL, '蒙古西征', 1219, '蒙古帝国扩张，征服欧亚', '成吉思汗及其子孙西征', '建立横跨欧亚的蒙古帝国', 'military', '/mengguxizheng.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (32, 12, NULL, '崖山海战', 1279, '元军追击南宋残余势力', '陆秀夫负帝昺跳海，南宋灭亡', '元朝统一全国', 'military', '/yashanhaizhan.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (33, 13, NULL, '郑和下西洋', 1405, '明朝强盛，宣扬国威', '郑和七次率船队远航西洋', '加强中外交流，展示明朝国力', 'cultural', '/zhenghexixiyang.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (34, 13, NULL, '土木堡之变', 1449, '明英宗亲征瓦剌', '明军在土木堡大败，英宗被俘', '明朝由盛转衰', 'military', '/tumubaozhibian.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (35, 13, NULL, '张居正改革', 1573, '明朝中后期，积弊日深', '张居正推行一条鞭法等改革', '明朝一度中兴', 'political', '/zhangjuzhenggai.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (36, 14, NULL, '雅克萨之战', 1685, '沙俄侵略黑龙江流域', '清军击败沙俄，签订尼布楚条约', '维护国家主权，划定边界', 'military', '/yakesazhizhan.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (37, 14, NULL, '鸦片战争', 1840, '英国以虎门销烟为借口发动战争', '英军攻占沿海城市，清军屡败', '中国开始沦为半殖民地半封建社会', 'military', '/ypzhanzheng.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (38, 14, NULL, '洋务运动', 1861, '西学东渐，自强求富', '洋务派创办近代企业和新式学堂', '开启中国近代化进程', 'political', '/yangwuyundong.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (39, 14, NULL, '甲午战争', 1894, '日本侵略朝鲜，进而侵华', '清军战败，北洋水师覆灭', '签订马关条约，民族危机加深', 'military', '/jiawuzhanzheng.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (40, 14, NULL, '戊戌变法', 1898, '民族危机加深，维新思想兴起', '光绪帝推行新政，百日而终', '思想启蒙，推动变革', 'political', '/wuxubianfa.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');
INSERT INTO `historical_event` VALUES (41, 14, NULL, '辛亥革命', 1911, '清廷腐败，革命浪潮兴起', '武昌起义爆发，各省纷纷独立', '结束封建帝制，建立民国', 'political', '/xinhaigeming.jpg', '2026-04-09 07:43:47', '2026-04-12 22:07:20');

-- ----------------------------
-- Table structure for historical_person
-- ----------------------------
DROP TABLE IF EXISTS `historical_person`;
CREATE TABLE `historical_person`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '人物ID，主键',
  `dynasty_id` bigint NOT NULL COMMENT '所属朝代ID',
  `person_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '人物姓名',
  `birth_year` int NULL DEFAULT NULL COMMENT '出生年份（公元前为负值）',
  `death_year` int NULL DEFAULT NULL COMMENT '去世年份（公元前为负值）',
  `identity_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '其他' COMMENT '身份类型（帝王/将相/文人/科学家/其他）',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `avatar_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像图片路径',
  `person_bio` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '人物生平简介',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_person_dynasty_id`(`dynasty_id` ASC) USING BTREE COMMENT '按所属朝代ID索引',
  INDEX `idx_person_name`(`person_name` ASC) USING BTREE COMMENT '按人物姓名索引',
  INDEX `idx_person_identity`(`identity_type` ASC) USING BTREE COMMENT '按身份类型索引',
  CONSTRAINT `historical_person_ibfk_1` FOREIGN KEY (`dynasty_id`) REFERENCES `dynasty` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 45 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '存储历史人物信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of historical_person
-- ----------------------------
INSERT INTO `historical_person` VALUES (1, 1, '大禹', -2070, -2060, '帝王', '[\"政治家\",\"治水英雄\"]', NULL, '夏朝开国君主，以治水功绩闻名，三过家门而不入', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (2, 1, '启', -2050, -2000, '帝王', '[\"政治家\"]', NULL, '夏朝第二位君主，大禹之子，开启世袭制', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (3, 2, '商汤', -1670, -1587, '帝王', '[\"政治家\",\"军事家\"]', NULL, '商朝开国君主，任用伊尹，推翻夏朝', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (4, 2, '伊尹', -1650, -1550, '政治家', '[\"政治家\",\"思想家\"]', NULL, '商朝开国功臣，辅佐商汤灭夏，被尊为\"元圣\"', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (5, 2, '盘庚', -1350, -1300, '帝王', '[\"政治家\"]', NULL, '商朝君主，迁都于殷，复兴商朝', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (6, 3, '周武王', -1087, -1043, '帝王', '[\"政治家\",\"军事家\"]', NULL, '周朝开国君主，牧野之战击败商纣王', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (7, 3, '姜子牙', -1100, -1015, '政治家', '[\"政治家\",\"军事家\"]', NULL, '辅佐周武王灭商，被封于齐国', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (8, 3, '周公旦', -1090, -1032, '政治家', '[\"政治家\",\"思想家\"]', NULL, '辅佐成王，平定三监之乱，制礼作乐', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (9, 4, '商鞅', -395, -338, '政治家', '[\"政治家\",\"改革家\"]', NULL, '秦国变法者，推行商鞅变法，使秦国富强', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (10, 4, '白起', -330, -257, '军事家', '[\"军事家\"]', NULL, '秦国名将，长平之战坑杀赵军40万', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (11, 4, '秦始皇', -259, -210, '帝王', '[\"政治家\",\"军事家\"]', NULL, '统一六国，建立中国第一个大一统王朝', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (12, 4, '李斯', -280, -208, '政治家', '[\"政治家\",\"文学家\"]', NULL, '秦朝丞相，协助秦始皇统一文字度量衡', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (13, 5, '刘邦', -256, -195, '帝王', '[\"政治家\",\"军事家\"]', NULL, '汉朝开国皇帝，楚汉之争击败项羽', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (14, 5, '项羽', -232, -202, '军事家', '[\"军事家\"]', NULL, '西楚霸王，力能扛鼎，乌江自刎', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (15, 5, '韩信', -230, -196, '军事家', '[\"军事家\"]', NULL, '汉初名将，被刘邦封为齐王，后被杀', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (16, 5, '张良', -250, -186, '政治家', '[\"政治家\",\"谋士\"]', NULL, '汉初谋士，运筹帷幄，辅佐刘邦统一天下', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (17, 5, '司马迁', -145, -86, '文人', '[\"史学家\",\"文学家\"]', NULL, '著《史记》，中国第一部纪传体通史', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (18, 5, '张骞', -164, -114, '外交家', '[\"外交家\",\"探险家\"]', NULL, '出使西域，开辟丝绸之路', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (19, 6, '曹操', 155, 220, '政治家', '[\"政治家\",\"军事家\",\"诗人\"]', NULL, '三国时期魏国奠基人，挟天子以令诸侯', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (20, 6, '刘备', 161, 223, '帝王', '[\"政治家\",\"军事家\"]', NULL, '蜀汉开国皇帝，以仁义著称', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (21, 6, '诸葛亮', 181, 234, '政治家', '[\"政治家\",\"军事家\",\"发明家\"]', NULL, '蜀汉丞相，鞠躬尽瘁，死而后已', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (22, 6, '关羽', 160, 220, '军事家', '[\"军事家\"]', NULL, '蜀汉名将，忠义化身，被尊为\"武圣\"', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (23, 6, '孙权', 182, 252, '帝王', '[\"政治家\",\"军事家\"]', NULL, '吴国开国皇帝，赤壁之战击败曹操', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (24, 7, '司马炎', 236, 290, '帝王', '[\"政治家\"]', NULL, '晋朝开国皇帝，统一三国', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (25, 7, '谢安', 320, 385, '政治家', '[\"政治家\",\"军事家\"]', NULL, '东晋政治家，淝水之战总指挥', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (26, 7, '王羲之', 303, 361, '文人', '[\"书法家\",\"文学家\"]', NULL, '东晋书法家，被誉为\"书圣\"', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (27, 8, '孝文帝', 467, 499, '帝王', '[\"政治家\",\"改革家\"]', NULL, '北魏皇帝，推行汉化改革', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (28, 9, '杨坚', 541, 604, '帝王', '[\"政治家\",\"军事家\"]', NULL, '隋朝开国皇帝，统一南北朝', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (29, 10, '李世民', 599, 649, '帝王', '[\"政治家\",\"军事家\"]', NULL, '唐太宗，开创贞观之治，被誉为千古一帝', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (30, 10, '魏征', 580, 643, '政治家', '[\"政治家\",\"谏臣\"]', NULL, '唐初名相，以直言敢谏闻名', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (31, 10, '李白', 701, 762, '文人', '[\"诗人\",\"文学家\"]', NULL, '唐代浪漫主义诗人，被誉为\"诗仙\"', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (32, 10, '杜甫', 712, 770, '文人', '[\"诗人\",\"文学家\"]', NULL, '唐代现实主义诗人，被誉为\"诗圣\"', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (33, 10, '武则天', 624, 705, '帝王', '[\"政治家\",\"女性\"]', NULL, '中国历史上唯一的女皇帝', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (34, 11, '赵匡胤', 927, 976, '帝王', '[\"政治家\",\"军事家\"]', NULL, '宋朝开国皇帝，杯酒释兵权', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (35, 11, '王安石', 1021, 1086, '政治家', '[\"政治家\",\"改革家\",\"文学家\"]', NULL, '北宋政治家、文学家，主持王安石变法', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (36, 11, '苏轼', 1037, 1101, '文人', '[\"文学家\",\"诗人\",\"书法家\"]', NULL, '北宋文学家，唐宋八大家之一', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (37, 11, '岳飞', 1103, 1142, '军事家', '[\"军事家\",\"民族英雄\"]', NULL, '南宋抗金名将，精忠报国', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (38, 12, '成吉思汗', 1162, 1227, '帝王', '[\"政治家\",\"军事家\"]', NULL, '蒙古帝国奠基者，一代天骄', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (39, 12, '忽必烈', 1215, 1294, '帝王', '[\"政治家\",\"军事家\"]', NULL, '元朝开国皇帝，建立元朝', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (40, 13, '朱元璋', 1328, 1398, '帝王', '[\"政治家\",\"军事家\"]', NULL, '明朝开国皇帝，从乞丐到皇帝', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (41, 13, '郑和', 1371, 1433, '外交家', '[\"外交家\",\"航海家\"]', NULL, '明代航海家，七下西洋', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (42, 14, '康熙', 1654, 1722, '帝王', '[\"政治家\",\"军事家\"]', NULL, '清朝皇帝，开创康乾盛世', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (43, 14, '林则徐', 1785, 1850, '政治家', '[\"政治家\",\"民族英雄\"]', NULL, '清朝政治家，虎门销烟的民族英雄', '2026-04-09 07:44:37', '2026-04-09 07:44:37');
INSERT INTO `historical_person` VALUES (44, 14, '孙中山', 1866, 1925, '政治家', '[\"政治家\",\"革命家\"]', NULL, '中国近代民主革命先行者，辛亥革命领导人', '2026-04-09 07:44:37', '2026-04-09 07:44:37');

-- ----------------------------
-- Table structure for note
-- ----------------------------
DROP TABLE IF EXISTS `note`;
CREATE TABLE `note`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '笔记ID，主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '笔记类型',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '笔记标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '富文本笔记内容',
  `tags` json NULL COMMENT '笔记标签（JSON数组）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_note_user_id`(`user_id` ASC) USING BTREE COMMENT '按用户ID索引',
  INDEX `idx_note_type`(`type` ASC) USING BTREE COMMENT '按笔记类型索引',
  CONSTRAINT `note_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '存储用户笔记信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of note
-- ----------------------------
INSERT INTO `note` VALUES (1, 1, 'study', '1', '', '[]', '2026-04-06 16:29:50', '2026-04-06 16:29:50');
INSERT INTO `note` VALUES (5, 1, 'study', '1', '', '[]', '2026-04-06 19:51:00', '2026-04-06 19:51:00');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码哈希',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像路径',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user' COMMENT '角色（admin/user）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1正常/0禁用）',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE COMMENT '用户名唯一索引',
  INDEX `idx_user_role`(`role` ASC) USING BTREE COMMENT '按用户角色索引',
  INDEX `idx_user_status`(`status` ASC) USING BTREE COMMENT '按用户状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '存储用户基本信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'test', '$2a$10$coWCUhSrP90Aq7qSKucy0.dlM0OPspi/n0I7vLpkD6CJFVBAFjAEK', 'tess@test.com', '/images/407c2274fbb54e13933df1245bdb319a.ico', 'user', 1, '2026-04-06 20:04:28', '2026-02-25 21:30:59', '2026-04-06 20:04:28');
INSERT INTO `user` VALUES (2, 'admin', '$2a$10$coWCUhSrP90Aq7qSKucy0.dlM0OPspi/n0I7vLpkD6CJFVBAFjAEK', 'admin@admin.com', '/images/407c2274fbb54e13933df1245bdb319a.ico', 'admin', 1, '2026-02-25 21:32:13', '2026-02-25 21:32:09', '2026-02-25 21:32:13');
INSERT INTO `user` VALUES (3, 'Elysia', '$2a$10$2XukX0yquh40HXXfwDyYLulu0yWnswjJbIQu8lXA2pJbdSzu5C2I.', 'Elysia@qq.com', '/images/8acc553d378349499d32d9e7540fba15.jpg', 'user', 1, '2026-04-06 19:33:05', '2026-03-12 07:22:08', '2026-04-06 19:33:05');
INSERT INTO `user` VALUES (4, 'seele', '$2a$10$w9fQALSoTjLS7UJCpyDaEuqcoW0oWgY5m7cBQMuwQqOroM0tXWbH2', 'seele@qq.com', '/images/default.png', 'user', 1, '2026-04-08 20:28:06', '2026-04-02 20:12:43', '2026-04-08 20:28:06');

-- ----------------------------
-- Table structure for question（趣味互动题库）
-- ----------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目ID，主键',
  `question_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'single' COMMENT '题型：single/multiple/judge',
  `topic_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'other' COMMENT '知识点来源：event/person/allusion/dynasty',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题干',
  `options` json NULL COMMENT '选项数组（JSON）',
  `answer` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '正确答案',
  `analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '答案解析',
  `difficulty` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'easy' COMMENT '难度：easy/medium/hard',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_question_topic`(`topic_type` ASC) USING BTREE,
  INDEX `idx_question_difficulty`(`difficulty` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '趣味互动题库表' ROW_FORMAT = Dynamic;
-- 说明：题目数据由后端 QuestionDataInitializer 在题库为空时自动写入内置题目，无需在此手工插入。

SET FOREIGN_KEY_CHECKS = 1;
