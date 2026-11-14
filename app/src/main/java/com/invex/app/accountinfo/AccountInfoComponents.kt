package com.invex.app.accountinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.invex.app.ui.components.SettingsSubHeader
import com.invex.app.ui.theme.FieldBorder
import com.invex.app.ui.theme.FieldFill
import com.invex.app.ui.theme.IconColor
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.PillGrey
import com.invex.app.ui.theme.SoftWhite

/** Shared Settings sub-page header with the "Account Information" title. */
@Composable
fun AccountInfoHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
    SettingsSubHeader(title = "Account Information", onBack = onBack, modifier = modifier)
}

/** Label over a 44dp rounded field: 24 + 8 + 44 = 76dp. BasicTextField keeps Material padding out of the way. */
@Composable
fun AccountField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = InvexType.FieldLabel, color = Color.Black)
        val shape = RoundedCornerShape(12.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(shape)
                .background(FieldFill)
                .border(1.dp, FieldBorder, shape)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = InvexType.Body16.copy(color = Color.Black),
                cursorBrush = SolidColor(Color.Black),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** SAVE (black) and CANCEL (grey), 60 high, equal width, 28 apart. */
@Composable
fun AccountActionButtons(onSave: () -> Unit, onCancel: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        AccountButton("SAVE", IconColor, SoftWhite, onSave, Modifier.weight(1f))
        AccountButton("CANCEL", PillGrey, Color.Black, onCancel, Modifier.weight(1f))
    }
}

@Composable
private fun AccountButton(text: String, fill: Color, textColor: Color, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(fill)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = InvexType.ExamButton, color = textColor)
    }
}
