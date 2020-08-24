/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2020 Micromata GmbH, Germany (www.micromata.com)
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

package org.projectforge.rest.fibu

import org.projectforge.business.fibu.*
import org.projectforge.rest.config.Rest
import org.projectforge.rest.core.AbstractDTOPagesRest
import org.projectforge.rest.dto.PostData
import org.projectforge.rest.dto.Rechnung
import org.projectforge.ui.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("${Rest.URL}/outgoingInvoice")
class RechnungPagesRest : AbstractDTOPagesRest<RechnungDO, Rechnung, RechnungDao>(RechnungDao::class.java, "fibu.rechnung.title") {

    @Autowired
    private lateinit var rechnungDao: RechnungDao

    @Autowired
    private lateinit var kundeDao: KundeDao

    @Autowired
    private lateinit var projektDao: ProjektDao

    @Autowired
    private lateinit var kontoDao: KontoDao

    /**
     * LAYOUT List page
     */
    override fun createListLayout(): UILayout {
        val layout = super.createListLayout()
                .add(UITable.createUIResultSetTable()
                        .add(lc, "nummer")
                        .add(UITableColumn("customer.name", title = "fibu.kunde"))
                        .add(UITableColumn("project.name", title = "fibu.projekt"))
                        .add(UITableColumn("konto.bezeichnung", title = "fibu.konto"))
                        .add(lc,"betreff", "datum", "faelligkeit", "bezahlDatum", "periodOfPerformanceBegin", "periodOfPerformanceEnd")
                        .add(UITableColumn("formattedNetSum", title = "fibu.common.netto"))
                        .add(UITableColumn("formattedGrossSum", title = "fibu.rechnung.bruttoBetrag"))
                        .add(UITableColumn("orders", title = "fibu.auftrag.auftraege", dataType = UIDataType.INT))
                        .add(lc, "bemerkung", "status"))
        layout.getTableColumnById("datum").formatter = Formatter.DATE
        layout.getTableColumnById("faelligkeit").formatter = Formatter.DATE
        layout.getTableColumnById("bezahlDatum").formatter = Formatter.DATE
        layout.getTableColumnById("periodOfPerformanceBegin").formatter = Formatter.DATE
        layout.getTableColumnById("periodOfPerformanceEnd").formatter = Formatter.DATE
        return LayoutUtils.processListPage(layout, this)
    }

    /**
     * LAYOUT Edit page
     */
    override fun createEditLayout(dto: Rechnung, userAccess: UILayout.UserAccess): UILayout {
        val layout = super.createEditLayout(dto, userAccess)
                .add(lc, "betreff")
                .add(UIRow()
                        .add(UICol()
                                .add(lc, "nummer", "typ"))
                        .add(UICol()
                                .add(lc, "status")
                                .add(UISelect.createAccountSelect(lc, "konto", multi = false)))
                        .add(UICol()
                                .add(lc, "datum", "vatAmountSum", "bezahlDatum", "faelligkeit"))
                        .add(UICol()
                                .add(lc, "netSum", "grossSum", "zahlBetrag", "discountMaturity", "discountPercent")))
                .add(UIRow()
                        .add(UICol()
                                .add(UISelect.createProjectSelect(lc, "project", false))
                                .add(UISelect.createCustomerSelect(lc, "customer", false))
                                .add(lc, "kundeText", "customerAddress", "customerref1", "attachment", "periodOfPerformanceBegin", "periodOfPerformanceEnd")))
                .add(UIRow()
                        .add(UICol()
                                .add(lc, "bemerkung"))
                        .add(UICol()
                                .add(lc, "besonderheiten")))
                // Positionen
                .add(UICustomized("invoice.outgoingPosition"))
        return LayoutUtils.processEditPage(layout, dto, this)
    }


    override fun transformForDB(dto: Rechnung): RechnungDO {
        val rechnungDO = RechnungDO()
        dto.copyTo(rechnungDO)
        return rechnungDO
    }

    override fun transformFromDB(obj: RechnungDO, editMode: Boolean): Rechnung {
        val rechnung = Rechnung()
        rechnung.copyFrom(obj)

        if(obj.projekt != null){
            rechnung.project!!.copyFrom(projektDao.getOrLoad(obj.projekt!!.id))
        }

        if(obj.kunde != null){
            rechnung.customer!!.copyFrom(kundeDao.getOrLoad(obj.kunde!!.id))
        }

        if(obj.konto != null){
            rechnung.konto!!.copyFrom(kontoDao.getOrLoad(obj.konto!!.id))
        }

        return rechnung
    }

    @PostMapping("addPosition")
    fun addPosition(request: HttpServletRequest, @RequestBody postData: PostData<Rechnung>) {
        val rechnung = RechnungDO()
        var newPosition = RechnungsPositionDO()
        postData.data.copyTo(rechnung)
        rechnung.projekt = ProjektDO()
        postData.data.project?.copyTo(rechnung.projekt!!)
        rechnung.kunde = KundeDO()
        postData.data.customer?.copyTo(rechnung.kunde!!)
        rechnung.konto= KontoDO()
        postData.data.konto?.copyTo(rechnung.konto!!)
        newPosition.rechnung = rechnung
        rechnung.addPosition(newPosition)
        rechnungDao.saveOrUpdate(rechnung)
    }
}
