package org.vaccineimpact.reporting_api.tests.db

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.reporting_api.ArtefactType
import org.vaccineimpact.reporting_api.parseArtefacts

class ParsingTests
{

    @Test
    fun `can parse Artefacts`()
    {
        val input = "[{\"data\":{\"description\":\"A summary table\"," +
                "\"filename\":[\"summary.csv\"]}},{\"staticgraph\"" +
                ":{\"description\":\"A summary graph\",\"filename\":[\"graph.png\"]}}]"

        val result = parseArtefacts(input)

        Assertions.assertThat(result.count()).isEqualTo(2)

        val data = result[0]

        Assertions.assertThat(data.type).isEqualTo(ArtefactType.DATA)
        Assertions.assertThat(data.files.count()).isEqualTo(1)
        Assertions.assertThat(data.files[0]).isEqualTo("summary.csv")
        Assertions.assertThat(data.description).isEqualTo("A summary table")
    }

    @Test
    fun `can parse Artefacts with single filename`()
    {
        val input = "[{\"data\":{\"description\":\"A summary table\"," +
                "\"filename\":\"summary.csv\"}}]"

        val result = parseArtefacts(input)

        val data = result[0]

        Assertions.assertThat(data.type).isEqualTo(ArtefactType.DATA)
        Assertions.assertThat(data.files.count()).isEqualTo(1)
        Assertions.assertThat(data.files[0]).isEqualTo("summary.csv")
        Assertions.assertThat(data.description).isEqualTo("A summary table")
    }
}