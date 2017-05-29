/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.gwt.elemento.sample.builder.client;

import elemental2.dom.Event;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.KeyboardEvent;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.sample.common.Application;
import org.jboss.gwt.elemento.sample.common.Filter;
import org.jboss.gwt.elemento.sample.common.I18n;
import org.jboss.gwt.elemento.sample.common.TodoItem;
import org.jboss.gwt.elemento.sample.common.TodoItemRepository;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.Elements.button;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.gwt.elemento.core.Elements.ul;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.change;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.gwt.elemento.core.EventType.keydown;
import static org.jboss.gwt.elemento.core.InputType.checkbox;
import static org.jboss.gwt.elemento.core.InputType.text;
import static org.jboss.gwt.elemento.sample.common.Filter.ACTIVE;
import static org.jboss.gwt.elemento.sample.common.Filter.ALL;
import static org.jboss.gwt.elemento.sample.common.Filter.COMPLETED;

class ApplicationElement implements IsElement {

    private static HTMLElement filter(Filter f, String text) {
        return li().add(a().apply(a -> a.href = f.fragment()).textContent(text)).asElement();
    }

    private final TodoItemRepository repository;
    private final I18n i18n;
    private Filter filter;

    private final HTMLElement root;
    private final HTMLInputElement newTodo;
    private final HTMLElement main;
    private final HTMLInputElement toggleAll;
    private final HTMLElement list;
    private final HTMLElement footer;
    private final HTMLElement count;
    private final HTMLElement filterAll;
    private final HTMLElement filterActive;
    private final HTMLElement filterCompleted;
    private final HTMLButtonElement clearCompleted;


    ApplicationElement(TodoItemRepository repository, I18n i18n) {
        this.repository = repository;

        this.root = section().css("todoapp")
                .add(header().css("header")
                        .add(h(1).textContent(i18n.constants().todos()))
                        .add(newTodo = input(text).css("new-todo").apply(input -> {
                            input.placeholder = i18n.constants().new_todo();
                            input.autofocus = true;
                        }).asElement()))
                .add(main = section().css("main")
                        .add(toggleAll = input(checkbox).css("toggle-all").id("toggle-all").asElement())
                        .add(label().attr("for", "toggle-all").textContent(i18n.constants().complete_all()))
                        .add(list = ul().css("todo-list").asElement())
                        .asElement())
                .add(footer = footer().css("footer")
                        .add(count = span().css("todo-count").innerHtml(i18n.messages().items(0)).asElement())
                        .add(ul().css("filters")
                                .add(filterAll = filter(ALL, i18n.constants().filter_all()))
                                .add(filterActive = filter(ACTIVE, i18n.constants().filter_active()))
                                .add(filterCompleted = filter(COMPLETED, i18n.constants().filter_completed())))
                        .add(clearCompleted = button()
                                .css("clear-completed")
                                .textContent(i18n.constants().clear_completed())
                                .asElement())
                        .asElement())
                .asElement();
        this.i18n = i18n;

        bind(newTodo, keydown, this::newTodo);
        bind(toggleAll, change, event -> toggleAll());
        bind(clearCompleted, click, event -> clearCompleted());

        reset();
        repository.onExternalModification(this::reset);
    }

    private void reset() {
        Elements.removeChildrenFrom(list);
        for (TodoItem item : repository.items()) {
            list.appendChild(new TodoItemElement(this, repository, item).asElement());
        }
        update();
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }


    // ------------------------------------------------------ event / token handler

    private void newTodo(Event event) {
        KeyboardEvent keyboardEvent = (KeyboardEvent) event;
        if ("Enter".equals(keyboardEvent.key)) {
            String text = newTodo.value.trim();
            if (text.length() != 0) {
                TodoItem item = repository.add(text);
                list.appendChild(new TodoItemElement(this, repository, item).asElement());
                newTodo.value = "";
                update();
            }
        }
    }

    private void toggleAll() {
        Application.toggleAll(list, toggleAll.checked);
        repository.completeAll(toggleAll.checked);
        update();
    }

    private void clearCompleted() {
        repository.removeAll(Application.getCompleted(list));
        update();
    }

    void filter(String token) {
        filter = Filter.parseToken(token);
        Application.filter(filter, filterAll, filterActive, filterCompleted);
        update();
    }


    // ------------------------------------------------------ state update

    void update() {
        Application.update(filter, i18n, list, main, footer, toggleAll, count, clearCompleted);
    }
}
