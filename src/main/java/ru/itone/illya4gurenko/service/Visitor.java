package ru.itone.illya4gurenko.service;

import ru.itone.illya4gurenko.exception.VisitorTypeException;

public interface Visitor {
    /**
     * Выполняет алгоритм генерации над переданным объектом.
     *
     * @param o Объект запроса
     * @throws VisitorTypeException Если передан объект некорректного типа
     */
    void visit(Object o) throws VisitorTypeException;
}
