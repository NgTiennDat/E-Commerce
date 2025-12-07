create table if not exists category (
                                        id            bigint          not null auto_increment,
                                        name          varchar(255)    not null,
                                        description   varchar(1000),
                                        slug          varchar(255)    not null,
                                        image_url     varchar(512),
                                        icon          varchar(255),
                                        is_active     bit             default b'1',
                                        display_order int,
                                        parent_id     bigint,

                                        created_by    varchar(50),
                                        created_at    datetime,
                                        updated_by    varchar(50),
                                        updated_at    datetime,
                                        is_deleted    bit             not null default b'0',

                                        primary key (id),
                                        unique key uk_category_slug (slug),
                                        constraint fk_category_parent foreign key (parent_id) references category(id)
);

create table if not exists products (
                                        id                 bigint          not null auto_increment,
                                        sku                varchar(100),
                                        name               varchar(255)    not null,
                                        short_description  varchar(255),
                                        description        text,
                                        available_quantity int             not null,
                                        price              decimal(38, 2)  not null,
                                        discount_percent   int,
                                        image_url          varchar(512),
                                        brand              varchar(255),
                                        rating             double,
                                        rating_count       int,
                                        is_featured        bit             default b'0',
                                        is_new             bit             default b'0',
                                        status             enum('ACTIVE','INACTIVE'),
                                        category_id        bigint,

    -- Audit columns
                                        created_by    varchar(50),
                                        created_at    datetime,
                                        updated_by    varchar(50),
                                        updated_at    datetime,
                                        is_deleted    bit             not null default b'0',

                                        primary key (id),
                                        constraint fk_products_category foreign key (category_id) references category(id)
);
