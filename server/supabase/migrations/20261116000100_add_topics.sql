-- Add two new topics without changing existing topic IDs.

insert into public.topics (
    code,
    title,
    description,
    is_active,
    sort_order
)
values
    (
        'animals',
        'Животные',
        'Домашние и дикие животные, птицы и насекомые',
        true,
        4
    ),
    (
        'transport',
        'Транспорт',
        'Автомобили и другие средства передвижения',
        true,
        5
    )
on conflict (code) do update
set
    title = excluded.title,
    description = excluded.description,
    is_active = true,
    sort_order = excluded.sort_order;