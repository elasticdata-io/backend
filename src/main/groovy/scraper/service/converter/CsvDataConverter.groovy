package scraper.service.converter

interface CsvDataConverter {
    String toCsv(List<HashMap> list)
}