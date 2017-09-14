package scraper.service.data.converter

interface CsvDataConverter {
    String toCsv(List<HashMap> list)
}