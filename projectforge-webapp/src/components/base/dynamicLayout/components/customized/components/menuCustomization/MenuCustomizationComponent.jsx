import React, { useState } from 'react';
import { closestCenter, DndContext, KeyboardSensor, PointerSensor, useSensor, useSensors } from '@dnd-kit/core';
import { arrayMove, SortableContext, sortableKeyboardCoordinates, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { MenuSortable } from './MenuSortable';
import { DynamicLayoutContext } from '../../../../context';

function MenuCustomizationComponent() {
    const { data, setData } = React.useContext(DynamicLayoutContext);
    const { favoritesMenu } = data;
    const { menuItems } = favoritesMenu;
    console.log(menuItems, setData);
    const [items, setItems] = useState(['1', '2', '3']);
    const sensors = useSensors(
        useSensor(PointerSensor),
        useSensor(KeyboardSensor, {
            coordinateGetter: sortableKeyboardCoordinates,
        }),
    );

    function handleDragEnd(event) {
        const { active, over } = event;

        if (active.id !== over.id) {
            setItems((newItems) => {
                const oldIndex = newItems.indexOf(active.id);
                const newIndex = newItems.indexOf(over.id);

                return arrayMove(newItems, oldIndex, newIndex);
            });
        }
    }

    return (
        // eslint-disable-next-line react/jsx-no-bind
        <DndContext
            sensors={sensors}
            collisionDetection={closestCenter}
            /* eslint-disable-next-line react/jsx-no-bind */
            onDragEnd={handleDragEnd}
        >
            <ul>
                <SortableContext
                    items={menuItems}
                    strategy={verticalListSortingStrategy}
                >
                    {menuItems.map((menuItem) => <MenuSortable key={menu} id={id} />)}
                </SortableContext>
            </ul>
        </DndContext>
    );
}

export default MenuCustomizationComponent;
