/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2022 Micromata GmbH, Germany (www.micromata.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.framework.persistence.history;

import de.micromata.genome.db.jpa.history.api.*;
import de.micromata.genome.db.jpa.history.entities.EntityOpType;
import de.micromata.genome.db.jpa.history.impl.HistoryEmgrAfterInsertedEventHandler;
import de.micromata.genome.db.jpa.history.impl.HistoryUpdateCopyFilterEventListener;
import de.micromata.genome.jpa.DbRecord;
import de.micromata.genome.jpa.events.EmgrAfterInsertedEvent;
import de.micromata.genome.jpa.events.EmgrUpdateCopyFilterEvent;
import de.micromata.genome.util.runtime.ClassUtils;
import de.micromata.hibernate.history.delta.PropertyDelta;
import de.micromata.hibernate.history.delta.SimplePropertyDelta;
import org.projectforge.business.user.UserGroupCache;
import org.projectforge.framework.configuration.ApplicationContextProvider;
import org.projectforge.framework.persistence.api.BaseDO;
import org.projectforge.framework.persistence.api.ExtendedBaseDO;
import org.projectforge.framework.persistence.api.ModificationStatus;
import org.projectforge.framework.persistence.api.PFPersistancyBehavior;
import org.projectforge.framework.persistence.jpa.PfEmgr;
import org.projectforge.framework.persistence.jpa.PfEmgrFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Utility to provide compat with BaseDao.
 *
 * @author Roger Rene Kommer (r.kommer.extern@micromata.de)
 */
public class HistoryBaseDaoAdapter {
  private static final Logger log = LoggerFactory.getLogger(HistoryBaseDaoAdapter.class);

  private static final HistoryEntry[] HISTORY_ARR_TEMPL = new HistoryEntry[]{};

  public static HistoryEntry[] getHistoryFor(BaseDO<?> obj) {
    //long begin = System.currentTimeMillis();
    HistoryEntry[] result = getHistoryEntries(obj).toArray(HISTORY_ARR_TEMPL);
    //long end = System.currentTimeMillis();
    //log.info("HistoryBaseDaoAdapter.getHistoryFor took: " + (end - begin) + " ms.");
    return result;
  }

  public static List<? extends HistoryEntry> getHistoryEntries(BaseDO<?> ob) {
    //long begin = System.currentTimeMillis();
    HistoryService histservice = HistoryServiceManager.get().getHistoryService();
    PfEmgrFactory emf = ApplicationContextProvider.getApplicationContext().getBean(PfEmgrFactory.class);
    List<? extends HistoryEntry> ret = emf.runRoTrans((emgr) -> {
      return histservice.getHistoryEntries(emgr, ob);
    });
    List<? extends HistoryEntry> nret = ret.stream()
            .sorted((e1, e2) -> e2.getModifiedAt().compareTo(e1.getModifiedAt())).collect(Collectors.toList());
    //long end = System.currentTimeMillis();
    //log.info("HistoryBaseDaoAdapter.getHistoryEntries took: " + (end - begin) + " ms.");
    return nret;
  }

  public static PropertyDelta diffEntryToPropertyDelta(DiffEntry de) {
    //long begin = System.currentTimeMillis();
    SimplePropertyDelta ret = new SimplePropertyDelta(de.getPropertyName(), String.class, de.getOldValue(),
            de.getNewValue());
    //long end = System.currentTimeMillis();
    //log.info("HistoryBaseDaoAdapter.diffEntryToPropertyDelta took: " + (end - begin) + " ms.");
    return ret;
  }

  public static List<SimpleHistoryEntry> getSimpleHistoryEntries(final BaseDO<?> ob, UserGroupCache userGroupCache) {
    //long begin = System.currentTimeMillis();
    List<SimpleHistoryEntry> ret = new ArrayList<>();
    List<? extends HistoryEntry> hel = getHistoryEntries(ob);

    for (HistoryEntry he : hel) {
      List<DiffEntry> deltas = he.getDiffEntries();
      if (deltas.isEmpty()) {
        SimpleHistoryEntry se = new SimpleHistoryEntry(userGroupCache, he);
        ret.add(se);
      } else {
        for (DiffEntry de : deltas) {
          final SimpleHistoryEntry se = new SimpleHistoryEntry(userGroupCache, he, diffEntryToPropertyDelta(de));
          ret.add(se);
        }
      }

    }
    //long end = System.currentTimeMillis();
    //log.info("HistoryBaseDaoAdapter.getSimpleHistoryEntries took: " + (end - begin) + " ms.");
    return ret;
  }

  public static boolean isHistorizable(Object bean) {
    if (bean == null) {
      return false;
    }
    return isHistorizable(bean.getClass());
  }

  public static boolean isHistorizable(Class<?> clazz) {
    return HistoryServiceManager.get().getHistoryService().hasHistory(clazz);
  }

  private static String histCollectionValueToString(Class<?> valueClass, Collection<?> value) {
    StringBuilder sb = new StringBuilder();
    for (Object ob : value) {
      if (sb.length() > 0) {
        sb.append(",");
      }
      if (ob instanceof DbRecord) {
        DbRecord rec = (DbRecord) ob;
        sb.append(rec.getPk());
      } else {
        sb.append(ob);
      }
    }
    return sb.toString();
  }

  private static String histValueToString(Class<?> valueClass, Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Collection) {
      return histCollectionValueToString(valueClass, (Collection) value);
    }
    return Objects.toString(value);
  }

  public static void createHistoryEntry(Object entity, Number id, String user, String property,
                                        Class<?> valueClass, Object oldValue, Object newValue) {
    createHistoryEntry(entity, id, EntityOpType.Update, user, property, valueClass, oldValue, newValue);
  }

  public static void createHistoryEntry(Object entity, Number id, EntityOpType opType, String user, String property,
                                        Class<?> valueClass, Object oldValue, Object newValue) {
    //long begin = System.currentTimeMillis();
    String oldVals = histValueToString(valueClass, oldValue);
    String newVals = histValueToString(valueClass, newValue);

    PfEmgrFactory emf = ApplicationContextProvider.getApplicationContext().getBean(PfEmgrFactory.class);
    emf.runInTrans((emgr) -> {
      HistoryServiceManager.get().getHistoryService().insertManualEntry(emgr, opType,
              entity.getClass().getName(),
              id, user, property, valueClass.getName(), oldVals, newVals);
      return null;
    });
    //long end = System.currentTimeMillis();
    //log.info("HistoryBaseDaoAdapter.createHistoryEntry took: " + (end - begin) + " ms.");
  }

  public static void inserted(BaseDO<?> ob) {
    //long begin = System.currentTimeMillis();
    PfEmgrFactory emf = ApplicationContextProvider.getApplicationContext().getBean(PfEmgrFactory.class);
    emf.runInTrans((emgr) -> {
      inserted(emgr, ob);
      return null;
    });
    //long end = System.currentTimeMillis();
    //log.info("HistoryBaseDaoAdapter.inserted took: " + (end - begin) + " ms.");
  }

  public static void inserted(PfEmgr emgr, BaseDO<?> ob) {
    EmgrAfterInsertedEvent event = new EmgrAfterInsertedEvent(emgr, ob);
    new HistoryEmgrAfterInsertedEventHandler().onEvent(event);
  }

  public static ModificationStatus wrapHistoryUpdate(BaseDO<?> dbo, Supplier<ModificationStatus> callback) {
    final HistoryService historyService = HistoryServiceManager.get().getHistoryService();
    final List<WithHistory> whanots = historyService.internalFindWithHistoryEntity(dbo);
    if (whanots.isEmpty()) {
      return callback.get();
    }
    final List<BaseDO<?>> entitiesToHistoricize = getSubEntitiesToHistoricizeDeep(dbo);
    final PfEmgrFactory emf = ApplicationContextProvider.getApplicationContext().getBean(PfEmgrFactory.class);
    final ModificationStatus result = emf.runInTrans((emgr) -> {
      return wrapHistoryUpdate(emgr, historyService, whanots, dbo, callback);
    });

    //long end = System.currentTimeMillis();
    //log.info("HistoryBaseDaoAdapter.wrappHistoryUpdate took: " + (end - begin) + " ms.");
    return result;
  }

  public static ModificationStatus wrapHistoryUpdate(PfEmgr emgr, BaseDO<?> dbo, Supplier<ModificationStatus> callback) {
    final HistoryService historyService = HistoryServiceManager.get().getHistoryService();
    final List<WithHistory> whanots = historyService.internalFindWithHistoryEntity(dbo);
    if (whanots.isEmpty()) {
      return callback.get();
    }
    return wrapHistoryUpdate(emgr, historyService, whanots, dbo, callback);
  }

  private static ModificationStatus wrapHistoryUpdate(PfEmgr emgr, HistoryService historyService, List<WithHistory> whanots, BaseDO<?> dbo, Supplier<ModificationStatus> callback) {
    final List<BaseDO<?>> entitiesToHistoricize = getSubEntitiesToHistoricizeDeep(dbo);
    final Map<Serializable, HistoryProperties> props = new HashMap<>();

    // get the (old) history properties before the modification
    entitiesToHistoricize.forEach(
            entity -> {
              final HistoryProperties p = getOrCreateHistoryProperties(props, entity);
              p.oldProps = historyService.internalGetPropertiesForHistory(emgr, whanots, entity);
            }
    );

    // do the modification
    final ModificationStatus result = callback.get();

    // get the (new) history properties after the modification
    entitiesToHistoricize.forEach(
            entity -> {
              final HistoryProperties p = getOrCreateHistoryProperties(props, entity);
              p.newProps = historyService.internalGetPropertiesForHistory(emgr, whanots, entity);
            }
    );

    // create history entries with the diff resulting from the old and new history properties
    props.forEach(
            (pk, p) -> {
              if (p.oldProps != null && p.newProps != null) {
                try {
                  historyService.internalOnUpdate(emgr, p.entClassName, pk, p.oldProps, p.newProps);
                } catch (Exception ex) {
                  log.error("Error while writing history entry (" + p.entClassName + ":" + pk + ", '" + p.oldProps + "'->'" + p.newProps + "': " + ex.getMessage(), ex);
                }
              }
            }
    );

    //long end = System.currentTimeMillis();
    //log.info("HistoryBaseDaoAdapter.wrappHistoryUpdate took: " + (end - begin) + " ms.");
    return result;
  }

  /**
   * Nested class just to hold some temporary history data.
   */
  private static final class HistoryProperties {
    private String entClassName;
    private Map<String, HistProp> oldProps;
    private Map<String, HistProp> newProps;
  }

  private static HistoryProperties getOrCreateHistoryProperties(final Map<Serializable, HistoryProperties> props, final DbRecord<?> entity) {
    final Serializable pk = entity.getPk();
    if (props.containsKey(pk)) {
      return props.get(pk);
    } else {
      final HistoryProperties hp = new HistoryProperties();
      props.put(pk, hp);
      hp.entClassName = entity.getClass().getName();
      return hp;
    }
  }

  private static List<BaseDO<?>> getSubEntitiesToHistoricizeDeep(final BaseDO<?> entity) {
    final List<BaseDO<?>> result = new ArrayList<>();
    final Queue<BaseDO<?>> queue = new LinkedList<>();
    queue.add(entity);

    // do a breadth first search through the tree
    while (!queue.isEmpty()) {
      final BaseDO<?> head = queue.poll();
      result.add(head);
      final List<BaseDO<?>> subEntries = getSubEntitiesToHistoricize(head);
      queue.addAll(subEntries);
    }

    return result;
  }

  /**
   * Takes a DO and returns a list of DOs. This list contains all entries of the collections of the DOs where the class fields have this annotation:
   * "@PFPersistancyBehavior(autoUpdateCollectionEntries = true)".
   *
   * @param entity The DO.
   * @return The List of DOs.
   */
  private static List<BaseDO<?>> getSubEntitiesToHistoricize(final BaseDO<?> entity) {
    final Collection<Field> fields = ClassUtils.getAllFields(entity.getClass()).values();
    AccessibleObject.setAccessible(fields.toArray(new Field[0]), true);

    return fields
            .stream()
            .filter(field -> {
              final PFPersistancyBehavior behavior = field.getAnnotation(PFPersistancyBehavior.class);
              return behavior != null && behavior.autoUpdateCollectionEntries();
            })
            .map(field -> {
              try {
                return (Collection<BaseDO<?>>) field.get(entity);
              } catch (IllegalAccessException | ClassCastException e) {
                return (Collection<BaseDO<?>>) Collections.EMPTY_LIST;
              }
            })
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
  }

  public static void updated(BaseDO<?> oldo, BaseDO<?> newo) {
    //long begin = System.currentTimeMillis();
    PfEmgrFactory emf = ApplicationContextProvider.getApplicationContext().getBean(PfEmgrFactory.class);
    emf.runInTrans((emgr) -> {
      EmgrUpdateCopyFilterEvent event = new EmgrUpdateCopyFilterEvent(emgr, oldo.getClass(), oldo.getClass(), oldo,
              newo,
              true);
      new HistoryUpdateCopyFilterEventListener().onEvent(event);
      return null;
    });
    //long end = System.currentTimeMillis();
    //log.info("HistoryBaseDaoAdapter.updated took: " + (end - begin) + " ms.");
  }

  public static void markedAsDeleted(ExtendedBaseDO<?> oldo, ExtendedBaseDO<?> newoj) {
    //long begin = System.currentTimeMillis();
    boolean prev = newoj.isDeleted();
    newoj.setDeleted(true);
    updated(oldo, newoj);
    newoj.setDeleted(prev);
    //long end = System.currentTimeMillis();
    //log.info("HistoryBaseDaoAdapter.markedAsDeleted took: " + (end - begin) + " ms.");
  }

  public static void markedAsUnDeleted(ExtendedBaseDO<?> oldo, ExtendedBaseDO<?> newoj) {
    //long begin = System.currentTimeMillis();
    boolean prev = newoj.isDeleted();
    newoj.setDeleted(false);
    updated(oldo, newoj);
    newoj.setDeleted(prev);
    //long end = System.currentTimeMillis();
    //log.info("HistoryBaseDaoAdapter.markedAsUnDeleted took: " + (end - begin) + " ms.");
  }
}
