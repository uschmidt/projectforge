import React from 'react';
import { useDraggable } from '@dnd-kit/core';
import PropTypes from 'prop-types';

function MenueDraggable(props) {
    const { children } = props;
    const {
        attributes, listeners, setNodeRef, transform,
    } = useDraggable({
        id: 'draggable',
    });
    const style = transform ? {
        transform: `translate3d(${transform.x}px, ${transform.y}px, 0)`,
    } : undefined;

    return (
        // eslint-disable-next-line react/button-has-type
        <button ref={setNodeRef} style={style} {...listeners} {...attributes}>
            {children}
        </button>
    );
}

MenueDraggable.propTypes = {
    children: PropTypes.oneOfType([
        PropTypes.arrayOf(PropTypes.node),
        PropTypes.node,
    ]).isRequired,
};

MenueDraggable.defaultProps = {};

export default MenueDraggable;
