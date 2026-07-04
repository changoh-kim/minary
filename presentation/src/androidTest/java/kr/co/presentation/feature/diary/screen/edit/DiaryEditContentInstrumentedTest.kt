package kr.co.presentation.feature.diary.screen.edit

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import kr.co.presentation.testing.BaseComposeTest
import kr.co.presentation.testing.PresentationComposeFixtures
import org.junit.Assert.assertTrue
import org.junit.Test

class DiaryEditContentInstrumentedTest : BaseComposeTest() {

    @Test
    fun inputAndSave_emitDiaryEditActions() {
        val actions = mutableListOf<DiaryEditAction>()

        setMinaryContent {
            DiaryEditContent(
                diary = PresentationComposeFixtures.diaryUiModel(
                    title = "",
                    content = "",
                ),
                onAction = actions::add,
            )
        }

        composeRule.onNodeWithTag("diary_edit_title_field")
            .performTextInput(PresentationComposeFixtures.TITLE)
        composeRule.onNodeWithTag("diary_edit_content_field")
            .performTextInput(PresentationComposeFixtures.CONTENT)
        composeRule.onNodeWithTag("diary_edit_save_button")
            .performClick()

        assertTrue(actions.contains(DiaryEditAction.TitleChanged(PresentationComposeFixtures.TITLE)))
        assertTrue(actions.contains(DiaryEditAction.ContentChanged(PresentationComposeFixtures.CONTENT)))
        assertTrue(actions.contains(DiaryEditAction.SaveClicked))
    }

    @Test
    fun savingState_disablesSaveButton() {
        setMinaryContent {
            DiaryEditContent(
                diary = PresentationComposeFixtures.diaryUiModel(),
                isSaving = true,
            )
        }

        composeRule.onNodeWithTag("diary_edit_save_button").assertIsNotEnabled()
    }
}
