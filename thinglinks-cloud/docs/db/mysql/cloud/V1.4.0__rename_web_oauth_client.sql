-- 将 Web OAuth 客户端标识与产品清单保持一致。
-- 仅迁移标准初始化记录；重复执行时不会再次修改数据。
UPDATE `def_client`
SET `client_id` = 'thinglinks_web',
    `client_secret` = 'thinglinks_web_secret',
    `name` = 'thinglinks_web',
    `updated_time` = CURRENT_TIMESTAMP
WHERE `id` = 1448881860003233792
  AND `client_id` = 'thinglinks_pro';
