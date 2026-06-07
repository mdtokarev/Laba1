
-- "if not exist" нужно, чтобы при повторном запуске проект не падал из-за конфликта таблиц, возможность создавать таблицу повторно
-- "bigserial" - тип bigint (=long), postgre сам выдает новые id; функция бд которая автоматически присваивает уникальный номер каждой новой строке
-- "primary key" - главный уникальный ключ таблицы
-- "varchar(64)" - строка до 64 символов
-- "references users(id)" - внешняя ссылка на таблицу пользователей, postgre следит, чтобы владелец существовал
-- "on delete cascade" - если удален эксперимент, то удалены все его прогоны
-- "double precision" - соответствует java double


create table if not exists users (
    id bigserial primary key,
    login varchar(64) not null unique,
    password_hash varchar(128) not null
);

create table if not exists experiments (
    id bigserial primary key,
    name varchar(128) not null,
    description varchar(512),
    owner_id bigint not null references users(id),
    created_at timestamp not null,
    updated_at timestamp not null
);

create table if not exists runs (
    id bigserial primary key,
    experiment_id bigint not null references experiments(id) on delete cascade,
    name varchar(128) not null,
    operator_name varchar(64) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table if not exists run_results (
    id bigserial primary key,
    run_id bigint not null references runs(id) on delete cascade,
    param varchar(32) not null,
    value double precision not null,
    unit varchar(16) not null,
    comment varchar(128),
    created_at timestamp not null,
    updated_at timestamp not null
);