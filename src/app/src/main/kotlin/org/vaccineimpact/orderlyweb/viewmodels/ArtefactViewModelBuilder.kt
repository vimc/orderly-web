package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.isImage
import org.vaccineimpact.orderlyweb.models.Artefact
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails

class ArtefactViewModelBuilder
{
    fun build(report: ReportVersionDetails, artefact: Artefact): ArtefactViewModel
    {
        val fileBuilder = DownloadableFileViewModelBuilder()
                .withReport(report)

        val inlineFileName = getInlineArtefactFile(artefact.files)
        val inline = if (inlineFileName == null)
        {
            null
        }
        else
        {
            fileBuilder
                    .withFileName(inlineFileName)
                    .inline()
                    .buildArtefactFileViewModel()
                    .url
        }

        val files = artefact.files.map {
            fileBuilder.withFileName(it)
                    .buildArtefactFileViewModel()
        }

        return ArtefactViewModel(artefact, files, inline)
    }

    private fun getInlineArtefactFile(files: List<String>): String?
    {
        //reproducing existing reportle behaviour - show the first file inline if it is an image
        return if (files.count() > 0 && isImage(files[0]))
        {
            files[0]
        }
        else
        {
            null
        }
    }
}
