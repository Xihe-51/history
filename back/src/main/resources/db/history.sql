create database history;
use history;
-- 1. 朝代基础信息表
CREATE TABLE `dynasty` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '朝代ID，主键',
                           `name` VARCHAR(100) NOT NULL COMMENT '朝代名称',
                           `start_year` INT NOT NULL COMMENT '起始年份（公元前为负值）',
                           `end_year` INT NOT NULL COMMENT '结束年份（公元前为负值）',
                           `capital` VARCHAR(200) DEFAULT NULL COMMENT '都城',
                           `founder` VARCHAR(100) DEFAULT NULL COMMENT '开国者/建立者',
                           `map_path` VARCHAR(500) DEFAULT NULL COMMENT '朝代地图路径',
                           `dynasty_desc` TEXT DEFAULT NULL COMMENT '朝代简介描述',
                           `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`),
                           INDEX `idx_dynasty_name` (`name`) COMMENT '按朝代名称索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='存储朝代基础信息表';

-- 2. 朝代细分阶段表
CREATE TABLE `dynasty_stage` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '阶段ID，主键',
                                 `dynasty_id` BIGINT NOT NULL COMMENT '所属朝代ID',
                                 `stage_name` VARCHAR(100) NOT NULL COMMENT '阶段名称（如：盛唐、晚唐）',
                                 `start_year` INT NOT NULL COMMENT '阶段起始年份',
                                 `end_year` INT NOT NULL COMMENT '阶段结束年份',
                                 `stage_desc` TEXT DEFAULT NULL COMMENT '阶段详细描述',
                                 `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`),
                                 INDEX `idx_dynasty_stage_dynasty_id` (`dynasty_id`) COMMENT '按所属朝代ID索引',
                                 FOREIGN KEY (`dynasty_id`) REFERENCES `dynasty`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='存储长跨度朝代细分阶段表';
-- dynasty表测试信息
INSERT INTO dynasty (name, start_year, end_year, capital, founder, map_path, dynasty_desc)
VALUES
    ('夏', -2070, -1600, '阳城', '禹', '/maps/xia.png', '中国史书记载的第一个王朝'),
    ('商', -1600, -1046, '殷', '汤', '/maps/shang.png', '以青铜文明著称'),
    ('周', -1046, -256, '镐京', '姬发', '/maps/zhou.png', '分为西周和东周'),
    ('秦', -221, -207, '咸阳', '嬴政', '/maps/qin.png', '中国第一个统一的中央集权王朝'),
    ('汉', -202, 220, '长安', '刘邦', '/maps/han.png', '奠定中华民族文化基础'),
    ('三国·魏', 220, 265, '洛阳', '曹丕', '/maps/wei.png', '三国时期的重要政权之一'),
    ('三国·蜀', 221, 263, '成都', '刘备', '/maps/shu.png', '蜀汉以刘备、诸葛亮著称'),
    ('三国·吴', 222, 280, '建业', '孙权', '/maps/wu.png', '江东基业'),
    ('唐', 618, 907, '长安', '李渊', '/maps/tang.png', '中国历史上最辉煌的时代之一'),
    ('宋', 960, 1279, '汴京', '赵匡胤', '/maps/song.png', '经济文化高度繁荣的时代');
-- dynasty_stage表测试信息
INSERT INTO dynasty_stage (dynasty_id, stage_name, start_year, end_year, stage_desc)
VALUES
    (1, '早夏', -2070, -1900, '夏朝早期阶段'),
    (1, '晚夏', -1899, -1600, '夏朝晚期阶段'),

    (2, '早商', -1600, -1400, '商朝早期'),
    (2, '晚商', -1399, -1046, '商朝晚期'),

    (3, '西周', -1046, -771, '礼乐文明高峰'),
    (3, '东周', -770, -256, '春秋战国时期'),

    (4, '秦统一前', -221, -214, '秦始皇统一六国前'),
    (4, '秦统一后', -213, -207, '秦朝统一后'),

    (5, '西汉', -202, 9, '汉朝前期'),
    (5, '东汉', 25, 220, '汉朝后期'),

    (6, '曹魏前期', 220, 240, '曹魏政权初期'),
    (6, '曹魏后期', 241, 265, '曹魏政权后期'),

    (7, '蜀汉前期', 221, 240, '刘备时期'),
    (7, '蜀汉后期', 241, 263, '诸葛亮时期'),

    (8, '东吴前期', 222, 250, '孙权前期'),
    (8, '东吴后期', 251, 280, '东吴后期'),

    (9, '盛唐', 618, 755, '唐朝最繁荣时期'),
    (9, '晚唐', 756, 907, '唐朝衰落时期'),

    (10, '北宋', 960, 1127, '宋朝前期'),
    (10, '南宋', 1128, 1279, '宋朝后期');

-- 3. 历史事件表
CREATE TABLE `historical_event` (
                                    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '事件ID，主键',
                                    `dynasty_id` BIGINT NOT NULL COMMENT '所属朝代ID',
                                    `stage_id` BIGINT DEFAULT NULL COMMENT '所属朝代阶段ID（可选）',
                                    `event_name` VARCHAR(200) NOT NULL COMMENT '事件名称',
                                    `event_year` INT NOT NULL COMMENT '事件核心年份（公元前为负值）',
                                    `event_bg` TEXT DEFAULT NULL COMMENT '事件背景描述',
                                    `event_process` TEXT DEFAULT NULL COMMENT '事件过程描述',
                                    `event_impact` TEXT DEFAULT NULL COMMENT '事件影响描述',
                                    `event_type` VARCHAR(50) NOT NULL DEFAULT '其他' COMMENT '事件类型（政治/军事/文化/经济/社会/其他）',
                                    `event_pic_path` VARCHAR(500) DEFAULT NULL COMMENT '事件配图路径',
                                    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    PRIMARY KEY (`id`),
                                    INDEX `idx_event_dynasty_id` (`dynasty_id`) COMMENT '按所属朝代ID索引',
                                    INDEX `idx_event_stage_id` (`stage_id`) COMMENT '按所属朝代阶段ID索引',
                                    INDEX `idx_event_year` (`event_year`) COMMENT '按事件年份索引',
                                    INDEX `idx_event_type` (`event_type`) COMMENT '按事件类型索引',
                                    FOREIGN KEY (`dynasty_id`) REFERENCES `dynasty`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                    FOREIGN KEY (`stage_id`) REFERENCES `dynasty_stage`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='存储核心历史事件信息表';

-- 4. 历史人物表
CREATE TABLE `historical_person` (
                                     `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '人物ID，主键',
                                     `dynasty_id` BIGINT NOT NULL COMMENT '所属朝代ID',
                                     `person_name` VARCHAR(100) NOT NULL COMMENT '人物姓名',
                                     `birth_year` INT DEFAULT NULL COMMENT '出生年份（公元前为负值）',
                                     `death_year` INT DEFAULT NULL COMMENT '去世年份（公元前为负值）',
                                     `identity_type` VARCHAR(50) NOT NULL DEFAULT '其他' COMMENT '身份类型（帝王/将相/文人/科学家/其他）',
                                     `tags` JSON DEFAULT NULL COMMENT '人物标签（JSON数组，如：["诗人","政治家"]）',
                                     `avatar_path` VARCHAR(500) DEFAULT NULL COMMENT '头像图片路径',
                                     `person_bio` TEXT DEFAULT NULL COMMENT '人物生平简介',
                                     `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     PRIMARY KEY (`id`),
                                     INDEX `idx_person_dynasty_id` (`dynasty_id`) COMMENT '按所属朝代ID索引',
                                     INDEX `idx_person_name` (`person_name`) COMMENT '按人物姓名索引',
                                     INDEX `idx_person_identity` (`identity_type`) COMMENT '按身份类型索引',
                                     FOREIGN KEY (`dynasty_id`) REFERENCES `dynasty`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='存储历史人物信息表';

-- 5. 典故表
CREATE TABLE `allusion` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '典故ID，主键',
                            `allusion_name` VARCHAR(200) NOT NULL COMMENT '典故名称',
                            `source` VARCHAR(300) DEFAULT NULL COMMENT '出处（古籍名称）',
                            `original_text` TEXT DEFAULT NULL COMMENT '原文',
                            `paraphrase` TEXT DEFAULT NULL COMMENT '释义',
                            `moral` TEXT DEFAULT NULL COMMENT '寓意',
                            `theme_type` VARCHAR(50) NOT NULL DEFAULT '其他' COMMENT '主题类型（励志/权谋/友情/忠诚/智慧/其他）',
                            `multimedia_path` VARCHAR(500) DEFAULT NULL COMMENT '多媒体资源路径（图片/音频/视频）',
                            `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            INDEX `idx_allusion_name` (`allusion_name`) COMMENT '按典故名称索引',
                            INDEX `idx_allusion_theme` (`theme_type`) COMMENT '按主题类型索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='存储经典典故信息表';

-- 6. 用户表
CREATE TABLE `user` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键',
                        `username` VARCHAR(50) NOT NULL COMMENT '用户名',
                        `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
                        `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
                        `avatar_path` VARCHAR(500) DEFAULT NULL COMMENT '头像路径',
                        `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色（admin/user）',
                        `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1正常/0禁用）',
                        `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
                        `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE INDEX `uk_username` (`username`) COMMENT '用户名唯一索引',
                        INDEX `idx_user_role` (`role`) COMMENT '按用户角色索引',
                        INDEX `idx_user_status` (`status`) COMMENT '按用户状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='存储用户基本信息表';

-- 7. 用户笔记表
CREATE TABLE `note` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '笔记ID，主键',
                        `user_id` BIGINT NOT NULL COMMENT '用户ID',
                        `type` VARCHAR(20) NOT NULL COMMENT '笔记类型',
                        `title` VARCHAR(200) NOT NULL COMMENT '笔记标题',
                        `content` LONGTEXT DEFAULT NULL COMMENT '富文本笔记内容',
                        `tags` JSON DEFAULT NULL COMMENT '笔记标签（JSON数组）',
                        `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        INDEX `idx_note_user_id` (`user_id`) COMMENT '按用户ID索引',
                        INDEX `idx_note_type` (`type`) COMMENT '按笔记类型索引',
                        FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='存储用户笔记信息表';