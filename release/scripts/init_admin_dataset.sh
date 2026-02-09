#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RELEASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPOSE_FILE="$RELEASE_DIR/docker-compose.yaml"
DATASET_SIZE="${DATASET_SIZE:-20}"

if ! [[ "$DATASET_SIZE" =~ ^[0-9]+$ ]] || (( DATASET_SIZE < 20 || DATASET_SIZE > 500 )); then
    echo "DATASET_SIZE 必须是 20 到 500 之间的整数" >&2
    exit 1
fi

compose() {
    docker compose -f "$COMPOSE_FILE" "$@"
}

wait_for_health() {
    local container="$1"
    local attempts="${2:-40}"
    local status

    for ((i = 1; i <= attempts; i++)); do
        status="$(docker inspect "$container" \
            --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' \
            2>/dev/null || true)"
        if [[ "$status" == "healthy" || "$status" == "running" ]]; then
            return 0
        fi
        sleep 3
    done

    echo "容器 $container 未能正常启动，当前状态：${status:-unknown}" >&2
    return 1
}

echo "启动 MySQL 和 Redis..."
compose up -d mysql redis
wait_for_health dawn-mysql
wait_for_health dawn-redis

MYSQL_ROOT_PASSWORD="$(compose exec -T mysql printenv MYSQL_ROOT_PASSWORD)"
MYSQL_DATABASE="$(compose exec -T mysql printenv MYSQL_DATABASE)"

mysql_exec() {
    compose exec -T \
        -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" \
        mysql mysql \
        --user=root \
        --default-character-set=utf8mb4 \
        "$MYSQL_DATABASE" "$@"
}

ADMIN_ID="$(mysql_exec --batch --skip-column-names --execute "
    SELECT ui.id
    FROM t_user_info ui
    INNER JOIN t_user_role ur ON ur.user_id = ui.id
    WHERE ur.role_id = 1
      AND ui.is_disable = 0
    ORDER BY ui.id
    LIMIT 1;
")"

if ! [[ "$ADMIN_ID" =~ ^[0-9]+$ ]]; then
    echo "未找到启用状态的管理员账号（role_id=1），无法注入数据" >&2
    exit 1
fi

echo "使用管理员 ID $ADMIN_ID 注入 $DATASET_SIZE 组数据..."

mysql_exec <<SQL
SET NAMES utf8mb4;
SET @admin_id := ${ADMIN_ID};
SET @dataset_size := ${DATASET_SIZE};

START TRANSACTION;

CREATE TEMPORARY TABLE seed_numbers (
    n INT PRIMARY KEY
);

INSERT INTO seed_numbers (n)
WITH RECURSIVE seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < @dataset_size
)
SELECT n FROM seq;

INSERT INTO t_category (category_name, create_time, update_time)
SELECT
    CONCAT('数据集分类', LPAD(s.n, 3, '0')),
    TIMESTAMPADD(DAY, -s.n, NOW()),
    NOW()
FROM seed_numbers s
WHERE NOT EXISTS (
    SELECT 1
    FROM t_category c
    WHERE c.category_name = CONCAT('数据集分类', LPAD(s.n, 3, '0'))
);

INSERT INTO t_tag (tag_name, create_time, update_time)
SELECT
    CONCAT('数据集标签', LPAD(s.n, 3, '0')),
    TIMESTAMPADD(DAY, -s.n, NOW()),
    NOW()
FROM seed_numbers s
WHERE NOT EXISTS (
    SELECT 1
    FROM t_tag t
    WHERE t.tag_name = CONCAT('数据集标签', LPAD(s.n, 3, '0'))
);

INSERT INTO t_article (
    user_id,
    category_id,
    article_cover,
    article_title,
    article_content,
    is_top,
    is_featured,
    is_delete,
    status,
    type,
    password,
    original_url,
    create_time,
    update_time
)
SELECT
    @admin_id,
    c.id,
    CONCAT('https://picsum.photos/seed/dawn-article-', LPAD(s.n, 3, '0'), '/1200/600'),
    CONCAT('[数据集] 管理员示例文章 ', LPAD(s.n, 3, '0')),
    CONCAT(
        '# 管理员示例文章 ', LPAD(s.n, 3, '0'), CHAR(10), CHAR(10),
        '这是由初始化脚本生成的公开文章，用于验证首页、归档、分类和标签页面。', CHAR(10), CHAR(10),
        '## 数据集说明', CHAR(10), CHAR(10),
        '- 创建者：管理员 ID ', @admin_id, CHAR(10),
        '- 序号：', s.n, CHAR(10),
        '- 用途：开发、演示与接口联调', CHAR(10), CHAR(10),
        '重复执行初始化脚本不会生成重复记录。'
    ),
    IF(s.n = 1, 1, 0),
    IF(s.n <= 6, 1, 0),
    0,
    1,
    1,
    NULL,
    NULL,
    TIMESTAMPADD(DAY, -s.n, NOW()),
    NOW()
FROM seed_numbers s
INNER JOIN t_category c
    ON c.category_name = CONCAT('数据集分类', LPAD(s.n, 3, '0'))
WHERE NOT EXISTS (
    SELECT 1
    FROM t_article a
    WHERE a.article_title = CONCAT('[数据集] 管理员示例文章 ', LPAD(s.n, 3, '0'))
);

INSERT INTO t_article_tag (article_id, tag_id)
SELECT a.id, t.id
FROM seed_numbers s
INNER JOIN t_article a
    ON a.article_title = CONCAT('[数据集] 管理员示例文章 ', LPAD(s.n, 3, '0'))
INNER JOIN t_tag t
    ON t.tag_name = CONCAT('数据集标签', LPAD(s.n, 3, '0'))
WHERE NOT EXISTS (
    SELECT 1
    FROM t_article_tag at
    WHERE at.article_id = a.id
      AND at.tag_id = t.id
);

INSERT INTO t_talk (
    user_id,
    content,
    images,
    is_top,
    status,
    create_time,
    update_time
)
SELECT
    @admin_id,
    CONCAT('[数据集] 管理员说说 ', LPAD(s.n, 3, '0'), '：记录开发、部署与内容运营进度。'),
    IF(
        MOD(s.n, 4) = 0,
        JSON_ARRAY(CONCAT('https://picsum.photos/seed/dawn-talk-', LPAD(s.n, 3, '0'), '/900/600')),
        NULL
    ),
    IF(s.n = 1, 1, 0),
    1,
    TIMESTAMPADD(HOUR, -s.n, NOW()),
    NOW()
FROM seed_numbers s
WHERE NOT EXISTS (
    SELECT 1
    FROM t_talk t
    WHERE t.content = CONCAT('[数据集] 管理员说说 ', LPAD(s.n, 3, '0'), '：记录开发、部署与内容运营进度。')
);

INSERT INTO t_comment (
    user_id,
    topic_id,
    comment_content,
    reply_user_id,
    parent_id,
    type,
    is_delete,
    is_review,
    create_time,
    update_time
)
SELECT
    @admin_id,
    NULL,
    CONCAT('[数据集] 留言板内容 ', LPAD(s.n, 3, '0'), '：欢迎访问 Dawn Blog。'),
    NULL,
    NULL,
    2,
    0,
    1,
    TIMESTAMPADD(HOUR, -s.n, NOW()),
    NOW()
FROM seed_numbers s
WHERE NOT EXISTS (
    SELECT 1
    FROM t_comment c
    WHERE c.type = 2
      AND c.comment_content = CONCAT('[数据集] 留言板内容 ', LPAD(s.n, 3, '0'), '：欢迎访问 Dawn Blog。')
);

INSERT INTO t_comment (
    user_id,
    topic_id,
    comment_content,
    reply_user_id,
    parent_id,
    type,
    is_delete,
    is_review,
    create_time,
    update_time
)
SELECT
    @admin_id,
    NULL,
    CONCAT('[数据集] About 互动内容 ', LPAD(s.n, 3, '0'), '：用于验证关于页面评论区。'),
    NULL,
    NULL,
    3,
    0,
    1,
    TIMESTAMPADD(HOUR, -s.n, NOW()),
    NOW()
FROM seed_numbers s
WHERE NOT EXISTS (
    SELECT 1
    FROM t_comment c
    WHERE c.type = 3
      AND c.comment_content = CONCAT('[数据集] About 互动内容 ', LPAD(s.n, 3, '0'), '：用于验证关于页面评论区。')
);

INSERT INTO t_friend_link (
    link_name,
    link_avatar,
    link_address,
    link_intro,
    create_time,
    update_time
)
SELECT
    CONCAT('数据集友链', LPAD(s.n, 3, '0')),
    CONCAT('https://picsum.photos/seed/dawn-friend-', LPAD(s.n, 3, '0'), '/200/200'),
    CONCAT('https://example.com/friend/', LPAD(s.n, 3, '0')),
    CONCAT('初始化脚本生成的第 ', s.n, ' 条演示友链'),
    TIMESTAMPADD(DAY, -s.n, NOW()),
    NOW()
FROM seed_numbers s
WHERE NOT EXISTS (
    SELECT 1
    FROM t_friend_link f
    WHERE f.link_name = CONCAT('数据集友链', LPAD(s.n, 3, '0'))
);

INSERT INTO t_photo_album (
    album_name,
    album_desc,
    album_cover,
    is_delete,
    status,
    create_time,
    update_time
)
SELECT
    CONCAT('数据集相册', LPAD(s.n, 3, '0')),
    CONCAT('初始化脚本生成的第 ', s.n, ' 个公开相册'),
    CONCAT('https://picsum.photos/seed/dawn-album-', LPAD(s.n, 3, '0'), '/800/500'),
    0,
    1,
    TIMESTAMPADD(DAY, -s.n, NOW()),
    NOW()
FROM seed_numbers s
WHERE NOT EXISTS (
    SELECT 1
    FROM t_photo_album pa
    WHERE pa.album_name = CONCAT('数据集相册', LPAD(s.n, 3, '0'))
);

INSERT INTO t_photo (
    album_id,
    photo_name,
    photo_desc,
    photo_src,
    is_delete,
    create_time,
    update_time
)
SELECT
    pa.id,
    CONCAT('数据集照片', LPAD(s.n, 3, '0')),
    CONCAT('相册 ', LPAD(s.n, 3, '0'), ' 的演示照片'),
    CONCAT('https://picsum.photos/seed/dawn-photo-', LPAD(s.n, 3, '0'), '/1200/800'),
    0,
    TIMESTAMPADD(DAY, -s.n, NOW()),
    NOW()
FROM seed_numbers s
INNER JOIN t_photo_album pa
    ON pa.album_name = CONCAT('数据集相册', LPAD(s.n, 3, '0'))
WHERE NOT EXISTS (
    SELECT 1
    FROM t_photo p
    WHERE p.album_id = pa.id
      AND p.photo_name = CONCAT('数据集照片', LPAD(s.n, 3, '0'))
);

INSERT INTO t_about (id, content, create_time, update_time)
SELECT
    1,
    JSON_OBJECT(
        'content',
        CAST(
            CONCAT(
                '# 关于 Dawn Blog', CHAR(10), CHAR(10),
                '这是管理员数据集初始化脚本生成的 About 页面内容。', CHAR(10), CHAR(10),
                '当前数据集包含文章、说说、标签、留言、友链、相册与照片，可用于页面展示和接口联调。'
            )
            AS CHAR CHARACTER SET utf8mb4
        )
    ),
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM t_about WHERE id = 1
);

UPDATE t_about
SET
    content = JSON_OBJECT(
        'content',
        CAST(
            CONCAT(
                '# 关于 Dawn Blog', CHAR(10), CHAR(10),
                '这是管理员数据集初始化脚本生成的 About 页面内容。', CHAR(10), CHAR(10),
                '当前数据集包含文章、说说、标签、留言、友链、相册与照片，可用于页面展示和接口联调。'
            )
            AS CHAR CHARACTER SET utf8mb4
        )
    ),
    update_time = NOW()
WHERE id = 1
  AND (
      content IS NULL
      OR content = ''
      OR JSON_UNQUOTE(JSON_EXTRACT(content, '$.content')) = 'this is about'
      OR JSON_UNQUOTE(JSON_EXTRACT(content, '$.content')) LIKE 'base64:type15:%'
      OR JSON_UNQUOTE(JSON_EXTRACT(content, '$.content')) LIKE '%管理员数据集初始化脚本生成%'
  );

DROP TEMPORARY TABLE seed_numbers;

COMMIT;
SQL

# About 内容由 Redis 缓存；清理该键使正在运行的应用读取新数据。
if [[ -z "${REDIS_PASSWORD:-}" ]]; then
    REDIS_PASSWORD="$(docker inspect dawn-redis \
        --format '{{range .Config.Cmd}}{{println .}}{{end}}' |
        awk 'previous == "--requirepass" { print; exit } { previous = $0 }')"
fi
REDIS_DATABASE="${REDIS_DATABASE:-1}"
compose exec -T redis redis-cli \
    --no-auth-warning \
    -a "$REDIS_PASSWORD" \
    -n "$REDIS_DATABASE" \
    DEL about >/dev/null

COUNTS="$(mysql_exec --batch --skip-column-names --execute "
    SELECT 'articles', COUNT(*) FROM t_article
      WHERE article_title LIKE '[数据集] 管理员示例文章 %'
    UNION ALL
    SELECT 'talks', COUNT(*) FROM t_talk
      WHERE content LIKE '[数据集] 管理员说说 %'
    UNION ALL
    SELECT 'tags', COUNT(*) FROM t_tag
      WHERE tag_name LIKE '数据集标签%'
    UNION ALL
    SELECT 'messages', COUNT(*) FROM t_comment
      WHERE type = 2 AND comment_content LIKE '[数据集] 留言板内容 %'
    UNION ALL
    SELECT 'about_comments', COUNT(*) FROM t_comment
      WHERE type = 3 AND comment_content LIKE '[数据集] About 互动内容 %'
    UNION ALL
    SELECT 'friend_links', COUNT(*) FROM t_friend_link
      WHERE link_name LIKE '数据集友链%'
    UNION ALL
    SELECT 'albums', COUNT(*) FROM t_photo_album
      WHERE album_name LIKE '数据集相册%'
    UNION ALL
    SELECT 'photos', COUNT(*) FROM t_photo
      WHERE photo_name LIKE '数据集照片%';
")"

while IFS=$'\t' read -r dataset count; do
    printf '%-16s %s\n' "$dataset" "$count"
    if (( count < DATASET_SIZE )); then
        echo "$dataset 数据量不足：期望至少 $DATASET_SIZE，实际 $count" >&2
        exit 1
    fi
done <<< "$COUNTS"

echo "管理员数据集初始化完成；重复执行不会生成重复记录。"
