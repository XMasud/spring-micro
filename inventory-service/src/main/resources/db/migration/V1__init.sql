CREATE TABLE `t_inventory`
(
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `sku_code` VARCHAR(255),
    `quantity` INT(11),
    PRIMARY KEY (`id`)
);