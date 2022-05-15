import React from 'react';
import { useDroppable } from '@dnd-kit/core';
import PropTypes from 'prop-types';

function MenuDroppable(props) {
    const { children } = props;

    const { isOver, setNodeRef } = useDroppable({
        id: 'droppable',
    });
    const style = {
        color: isOver ? 'green' : undefined,
    };

    return (
        <div ref={setNodeRef} style={style}>
            { children }
        </div>
    );
}

MenuDroppable.propTypes = {
    children: PropTypes.oneOfType([
        PropTypes.arrayOf(PropTypes.node),
        PropTypes.node,
    ]).isRequired,
};

MenuDroppable.defaultProps = {};

export default MenuDroppable;
