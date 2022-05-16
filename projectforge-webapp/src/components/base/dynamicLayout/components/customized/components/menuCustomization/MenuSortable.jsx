import React from 'react';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import PropTypes from 'prop-types';

export function MenuSortable(props) {
    const { id } = props;
    const {
        attributes,
        listeners,
        setNodeRef,
        transform,
        transition,
    } = useSortable({ id });

    const style = {
        transform: CSS.Transform.toString(transform),
        transition,
    };

    return (
        <div ref={setNodeRef} style={style} {...attributes} {...listeners}>
            {id}
        </div>
    );
}

MenuSortable.propTypes = {
    id: PropTypes.string,
};

MenuSortable.defaultProps = {
    id: undefined,
};

export default MenuSortable;
