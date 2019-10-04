package scraper.service.service.converter

import org.springframework.stereotype.Service

@Service
class CsvConverter implements CsvDataConverter {

    String toCsv(List<HashMap> list) {
        String responseData = ''
        HashSet columns = new HashSet()
        list.each { map ->
            map.each {k, v -> columns.add(k)}
        }
        def encode = { e -> e == null ? '' : e instanceof String ? /"$e"/ : "$e" }
        responseData += columns.collect { c -> encode( c ) }.join( ',' )
        responseData += '\n'
        responseData += list.collect { row ->
            columns.collect { colName -> encode( row[ colName ] ) }.join( ',' )
        }.join( '\n' )
        return responseData
    }
}
