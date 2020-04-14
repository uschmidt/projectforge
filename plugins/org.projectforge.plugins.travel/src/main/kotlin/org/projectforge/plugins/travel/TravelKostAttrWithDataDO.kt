package org.projectforge.plugins.travel

import de.micromata.genome.db.jpa.tabattr.entities.JpaTabAttrDataBaseDO
import org.projectforge.business.fibu.EmployeeAttrDataDO
import javax.persistence.*

@Entity
@DiscriminatorValue("1")
class TravelKostAttrWithDataDO : TravelKostAttrDO {
    constructor() : super()

    constructor(parent: TravelKostDO, propertyName: String, type: Char, value: String) : super(parent, propertyName, type, value)

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "parent", targetEntity = EmployeeAttrDataDO::class, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderColumn(name = "datarow")
    override fun getData(): List<JpaTabAttrDataBaseDO<*, Int>> {
        return super.getData()
    }
}