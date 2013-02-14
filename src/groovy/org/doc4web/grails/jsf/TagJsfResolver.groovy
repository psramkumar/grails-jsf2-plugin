package org.doc4web.grails.jsf

import javax.faces.model.SelectItem

/**
 * org.doc4web.grails.jsf
 *
 * Date: 20 mars 2010
 * Time: 15:03:22
 *
 * @author Stephane MALDINI
 */

class TagJsfResolver {

  def gspTagLibraryLookup
  final shell = new GroovyShell()

  final groov = [
          get: {key ->
            return shell.evaluate(key)
          }
  ] as Map

  final range = [
          get: {key ->
          }
  ] as Map

  final availableCurrencies = ['EUR', 'XCD', 'USD', 'XOF', 'NOK', 'AUD', 'XAF', 'NZD', 'MAD', 'DKK', 'GBP', 'CHF', 'XPF', 'ILS', 'ROL', 'TRL'].collect { new SelectItem(it.toString()) }
  final availableLocales = Locale.getAvailableLocales().collect { new SelectItem(it.toString()) }
  final availableTimezones = TimeZone.getAvailableIDs().collect { new SelectItem(it.toString()) }


}