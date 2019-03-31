import PropTypes from 'prop-types';

export const menuItemPropType = PropTypes.shape({
    title: PropTypes.string,
});

export const categoryItemPropType = PropTypes.shape({
    name: PropTypes.string,
    url: PropTypes.string,
});

export const categoryPropType = PropTypes.shape({
    name: PropTypes.string,
    items: PropTypes.arrayOf(categoryItemPropType),
});

export const buttonPropType = PropTypes.shape({
    style: PropTypes.oneOf(['primary', 'secondary', 'success', 'info', 'warning', 'danger', 'link']),
    title: PropTypes.string,
    id: PropTypes.string,
    handleClick: PropTypes.func,
    type: PropTypes.oneOf(['BUTTON', 'CHECKBOX']),
    checked: PropTypes.bool,
});

export const colorPropType = PropTypes.oneOf([
    'primary',
    'secondary',
    'success',
    'danger',
    'warning',
    'info',
]);

export const selectProps = {
    id: PropTypes.string.isRequired,
    color: colorPropType,
    label: PropTypes.string,
    options: PropTypes.oneOfType([
        PropTypes.arrayOf(PropTypes.shape({
            value: PropTypes.string,
            title: PropTypes.string,
        })),
        PropTypes.arrayOf(PropTypes.oneOfType([
            PropTypes.string,
            PropTypes.number,
        ])),
    ]).isRequired,
};

export const dataPropType = PropTypes.objectOf(PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number,
    PropTypes.bool,
    PropTypes.objectOf(PropTypes.oneOfType([
        PropTypes.string,
        PropTypes.number,
        PropTypes.bool,
    ])),
    PropTypes.arrayOf(PropTypes.shape({})),
]));
