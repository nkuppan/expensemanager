package com.nkuppan.expensemanager.data.mappers

/**
 * This interface abstraction is used to do a common functions to convert the data layer model to
 * domain layer model. This domain layer model will be used in the presentation layer.
 *
 * @param FromObject Data layer model (Network or Local database model, etc)
 * @param ToObject Domain layer model (UI Model or business logic model)
 */
interface Mapper<in FromObject, ToObject> {

    fun convert(fromObject: FromObject): ToObject
}