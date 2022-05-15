import React from 'react';
import { DndContext } from '@dnd-kit/core';
import MenuDraggable from './MenuDraggable';
import MenuDroppable from './MenuDroppable';

function MenuCustomizationComponent() {
    const [isDropped, setIsDropped] = React.useState(false);
    const draggableMarkup = (
        <MenuDraggable>Drag me</MenuDraggable>
    );

    function handleDragEnd(event) {
        if (event.over && event.over.id === 'droppable') {
            setIsDropped(true);
        }
    }

    return (
        // eslint-disable-next-line react/jsx-no-bind
        <DndContext onDragEnd={handleDragEnd}>
            {!isDropped ? draggableMarkup : null}
            <MenuDroppable>
                {isDropped ? draggableMarkup : 'Drop here'}
            </MenuDroppable>
        </DndContext>
    );
}

export default MenuCustomizationComponent;
