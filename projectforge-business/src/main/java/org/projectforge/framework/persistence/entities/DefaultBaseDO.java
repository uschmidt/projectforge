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

package org.projectforge.framework.persistence.entities;

import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.hibernate.search.annotations.Analyzer;
import org.projectforge.common.anots.PropertyInfo;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;


/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@MappedSuperclass
@Analyzer(impl = ClassicAnalyzer.class)
public class DefaultBaseDO extends AbstractHistorizableBaseDO<Integer> {
  private static final long serialVersionUID = 659687830219996653L;

  @PropertyInfo(i18nKey = "id")
  private Integer id;

  @Override
  @Id
  @GeneratedValue
  @Column(name = "pk")
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(final Integer id) {
    this.id = id;
  }
}
