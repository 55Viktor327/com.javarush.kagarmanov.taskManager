package com.javarush.model.entity.enums;

public enum Role {
    SUPER_ADMIN,   // Полный доступ ко всему + управление пользователями
    ADMIN,         // Управление всеми задачами и пользователями в своей команде
    USER,          // Работа со своими задачами
    GUEST          // Только просмотр (без изменений)
}
