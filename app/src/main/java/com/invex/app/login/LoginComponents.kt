package com.invex.app.login

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.invex.app.R
import com.invex.app.ui.components.LineUnderlinedText
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.PillGrey
import com.invex.app.ui.theme.SoftWhite
import com.invex.app.ui.theme.TableHeaderText
import com.invex.app.ui.theme.WarningRed

/** "InvEx" wordmark over "Already have an account ? Sign up". 80dp tall: 48 + 8 + 24. */
@Composable
fun LoginHeader(onSignUp: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("InvEx", style = InvexType.LoginWordmark, color = Color.Black)
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Already have an account ?", style = InvexType.LoginSubtext, color = Color.Black)
            LineUnderlinedText(
                text = "Sign up",
                style = InvexType.LoginSubtext,
                color = Color.Black,
                underlineColor = Orange,
                underlineOffset = 4.dp,
                modifier = Modifier.clickable(onClick = onSignUp),
            )
        }
    }
}

/** Grey 136 x 40dp pill ("Faculty" with an up/down arrow). Tapping opens a menu of roles. */
@Composable
fun RoleSelector(
    selected: UserRole,
    onSelect: (UserRole) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            modifier = Modifier
                .width(136.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PillGrey)
                .clickable { expanded = true },
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(selected.label, style = InvexType.RoleLabel, color = Color.Black)
            Image(
                painter = painterResource(R.drawable.ic_unfold_more),
                contentDescription = "Choose role",
                modifier = Modifier.size(16.dp),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            UserRole.entries.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role.label, style = InvexType.Body16) },
                    onClick = {
                        onSelect(role)
                        expanded = false
                    },
                )
            }
        }
    }
}

/**
 * Label with an orange line under it, over a 40dp outlined field: 24 + 8 + 40 = 72dp.
 * The field is a BasicTextField so Material's default TextField padding and height can't interfere.
 */
@Composable
fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LineUnderlinedText(
            text = label,
            style = InvexType.FieldLabel,
            color = Color.Black,
            underlineColor = Orange,
            underlineOffset = 4.dp,
        )
        val shape = RoundedCornerShape(8.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .border(1.dp, TableHeaderText, shape)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = InvexType.Body16.copy(color = Color.Black),
                cursorBrush = SolidColor(Color.Black),
                visualTransformation = visualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Orange full-width Login button, 40dp tall. */
@Composable
fun LoginButton(onClick: () -> Unit, modifier: Modifier = Modifier, loading: Boolean = false) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Orange)
            .clickable(enabled = !loading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(if (loading) "Logging in..." else "Login", style = InvexType.LoginButton, color = SoftWhite)
    }
}

/** Red message above the button (wrong password, no internet...). Not in the Figma frame yet. */
@Composable
fun LoginError(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        style = InvexType.LoginSubtext,
        color = WarningRed,
        modifier = modifier.fillMaxWidth(),
    )
}

/** Small legal line under the button. Left aligned (the Figma frame was justified). */
@Composable
fun TermsText(modifier: Modifier = Modifier) {
    Text(
        text = "By clicking continue, you agree to our Terms of Service and Privacy Policy.",
        style = InvexType.Terms,
        color = TableHeaderText,
        modifier = modifier.width(248.dp),
    )
}
