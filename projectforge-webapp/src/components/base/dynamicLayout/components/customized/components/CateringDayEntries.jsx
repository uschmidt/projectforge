/* eslint-disable no-param-reassign */
import PropTypes from 'prop-types';
import React from 'react';

function CateringDayEntries({ entries }) {
    function setBreakfast(event, dayNumber) {
        // console.log(event.target.checked);
        entries = entries.map((item) => {
            if (item.dayNumber === dayNumber) {
                const updatedItem = {
                    ...item,
                    breakfast: event.target.checked,
                };

                return updatedItem;
            }

            return item;
        });
    }

    function setLunch(event, dayNumber) {
        // console.log(event.target.checked);
        entries = entries.map((item) => {
            if (item.dayNumber === dayNumber) {
                const updatedItem = {
                    ...item,
                    lunch: event.target.checked,
                };

                return updatedItem;
            }

            return item;
        });
    }

    function setDinner(event, dayNumber) {
        // console.log(event.target.checked);
        entries = entries.map((item) => {
            if (item.dayNumber === dayNumber) {
                const updatedItem = {
                    ...item,
                    dinner: event.target.checked,
                };

                return updatedItem;
            }

            return item;
        });
    }

    return (
        <React.Fragment>
            {entries && entries.length > 0 && entries.map((entry, index) => (
                <tr>
                    <td>{entry.dayNumber}</td>
                    <td>{entry.date}</td>
                    <td>
                        <input
                            type="checkbox"
                            defaultValue={entry.breakfast}
                            onChange={event => setBreakfast(event, entry.dayNumber)}
                        />
                    </td>
                    <td>
                        <input
                            type="checkbox"
                            defaultValue={entry.lunch}
                            onChange={event => setLunch(event, entry.dayNumber)}
                        />
                    </td>
                    <td>
                        <input
                            type="checkbox"
                            defaultValue={entry.dinner}
                            onChange={event => setDinner(event, entry.dayNumber)}
                        />
                    </td>
                </tr>
            ))}
        </React.Fragment>
    );
}

CateringDayEntries.propTypes = {
    entries: PropTypes.arrayOf(PropTypes.shape({
        dayNumber: PropTypes.number,
        dateFormatted: PropTypes.string,
        breakfast: PropTypes.bool,
        lunch: PropTypes.bool,
        dinner: PropTypes.bool,
    })),
};

CateringDayEntries.defaultProps = {
    entries: undefined,
};

export default CateringDayEntries;
