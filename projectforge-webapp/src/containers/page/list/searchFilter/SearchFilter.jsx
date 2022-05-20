import { faSearch, faSync } from '@fortawesome/free-solid-svg-icons';
import { faFileExcel } from '@fortawesome/free-regular-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import classNames from 'classnames';
import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import { Button, Navbar } from 'reactstrap';
import {
    createListFavorite,
    deleteListFavorite,
    dismissCurrentError,
    fetchCurrentList,
    exportCurrentList,
    openEditPage,
    renameListFavorite,
    selectListFavorite,
    updateListFavorite,
} from '../../../../actions';
import { changeSearchString } from '../../../../actions/list/filter';
import Navigation from '../../../../components/base/navigation';
import { Alert, Col, Spinner } from '../../../../components/design';
import TextAutoCompletion
    from '../../../../components/design/input/autoCompletion/TextAutoCompletion';
import AdvancedPopperAction from '../../../../components/design/popper/AdvancedPopperAction';
import FavoritesPanel from '../../../panel/favorite/FavoritesPanel';
import styles from '../ListPage.module.scss';
import MagicFilters from './magicFilter/MagicFilters';

function SearchFilter(props) {
    const {
        category,
        onErrorDismiss,
        onFavoriteCreate,
        onFavoriteDelete,
        onFavoriteRename,
        onFavoriteSelect,
        onFavoriteUpdate,
        onSearchStringBlur,
        onSearchStringChange,
        onSearchStringDelete,
        onSelectQuickSelection,
        onSyncButtonClick,
        onExportButtonClick,
    } = props;

    const {
        error,
        filter,
        filterFavorites,
        isFetching,
        newlySwitched,
        quickSelectUrl,
        ui,
    } = category;

    return (
        <>
            <div className={styles.searchRow}>
                {/* FLEX-BOX IS SET TO REVERSE ON BIG SCREENS */}
                <div className={classNames(styles.container, styles.flex)}>
                    {/* Render the menu if it's loaded. */}
                    {ui && ui.pageMenu && (
                        <Col>
                            <Navbar>
                                <Navigation
                                    entries={ui.pageMenu}
                                    // Let the menu float to the right.
                                    className="ml-auto"
                                />
                            </Navbar>
                        </Col>
                    )}
                </div>
                <div className={styles.container}>
                    <FavoritesPanel
                        onFavoriteCreate={onFavoriteCreate}
                        onFavoriteDelete={onFavoriteDelete}
                        onFavoriteRename={onFavoriteRename}
                        onFavoriteSelect={onFavoriteSelect}
                        onFavoriteUpdate={onFavoriteUpdate}
                        favorites={filterFavorites}
                        currentFavoriteId={filter.id}
                        isModified
                        closeOnSelect={false}
                        translations={ui.translations}
                        htmlId="searchFilterFavoritesPopover"
                    />
                    {ui && ui.excelExportSupported && (
                        <Button
                            id="excelExport"
                            color="link"
                            className="selectPanelIconLinks"
                        >
                            <FontAwesomeIcon
                                icon={faFileExcel}
                                size="lg"
                                title={ui.translations.exportAsXls}
                                onClick={onExportButtonClick}
                            />
                        </Button>
                    )}
                    {isFetching && <Spinner className={styles.loadingSpinner} />}
                </div>
                <TextAutoCompletion
                    actions={(
                        <AdvancedPopperAction
                            type="delete"
                            disabled={!filter.searchString}
                            onClick={onSearchStringDelete}
                        >
                            {ui.translations.delete || ''}
                        </AdvancedPopperAction>
                    )}
                    className={styles.searchContainer}
                    inputId="searchString"
                    inputProps={{
                        autoFocus: newlySwitched,
                        icon: faSearch,
                        noStyle: true,
                        onBlur: onSearchStringBlur,
                        placeholder: ui.translations.search || '',
                        selectOnFocus: newlySwitched,
                        children: (
                            <FontAwesomeIcon
                                icon={faSync}
                                className={styles.syncButton}
                                onClick={onSyncButtonClick}
                            />
                        ),
                    }}
                    onChange={onSearchStringChange}
                    onSelect={onSelectQuickSelection}
                    url={quickSelectUrl}
                    value={filter.searchString}
                    withInput={false}
                />
            </div>
            <MagicFilters />
            <hr />
            <Alert
                color="danger"
                className={styles.alert}
                toggle={onErrorDismiss}
                isOpen={error !== undefined}
            >
                <h4>Oh Snap!</h4>
                <p>Error while contacting the server. Please contact an administrator.</p>
            </Alert>
        </>
    );
}

SearchFilter.propTypes = {
    category: PropTypes.shape({
        ui: PropTypes.shape({
            translations: PropTypes.shape({
                search: PropTypes.string,
                delete: PropTypes.string,
                exportAsXls: PropTypes.string,
            }),
            title: PropTypes.string,
            pageMenu: PropTypes.shape({}),
            excelExportSupported: PropTypes.bool,
        }),
        filter: PropTypes.shape({
            id: PropTypes.string,
            searchString: PropTypes.string,
        }),
        filterFavorites: PropTypes.arrayOf(PropTypes.shape({})),
        error: PropTypes.string,
        isFetching: PropTypes.bool,
        newlySwitched: PropTypes.bool,
        quickSelectUrl: PropTypes.string,
    }).isRequired,
    onErrorDismiss: PropTypes.func.isRequired,
    onFavoriteCreate: PropTypes.func.isRequired,
    onFavoriteDelete: PropTypes.func.isRequired,
    onFavoriteRename: PropTypes.func.isRequired,
    onFavoriteSelect: PropTypes.func.isRequired,
    onFavoriteUpdate: PropTypes.func.isRequired,
    onSearchStringBlur: PropTypes.func.isRequired,
    onSearchStringChange: PropTypes.func.isRequired,
    onSearchStringDelete: PropTypes.func.isRequired,
    onSelectQuickSelection: PropTypes.func.isRequired,
    onSyncButtonClick: PropTypes.func.isRequired,
    onExportButtonClick: PropTypes.func.isRequired,
};

SearchFilter.defaultProps = {};

const mapStateToProps = ({ list }) => {
    const category = list.categories[list.currentCategory];

    return {
        category,
    };
};

const actions = (dispatch) => ({
    onErrorDismiss: () => dispatch(dismissCurrentError()),
    onFavoriteCreate: (name) => dispatch(createListFavorite({ name })),
    onFavoriteDelete: (id) => dispatch(deleteListFavorite({ id })),
    onFavoriteRename: (id, newName) => dispatch(renameListFavorite({
        id,
        newName,
    })),
    onFavoriteSelect: (id) => dispatch(selectListFavorite({ id })),
    onFavoriteUpdate: () => dispatch(updateListFavorite()),
    onSearchStringBlur: () => dispatch(fetchCurrentList()),
    onSearchStringChange: (completion) => dispatch(changeSearchString(completion)),
    onSearchStringDelete: () => dispatch(changeSearchString('')),
    onSelectQuickSelection: ({ id }) => dispatch(openEditPage(id)),
    onSyncButtonClick: () => dispatch(fetchCurrentList(true)),
    onExportButtonClick: () => dispatch(exportCurrentList()),
});

export default connect(mapStateToProps, actions)(SearchFilter);
