package com.invex.app.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.invex.app.R
import com.invex.app.login.UserRole
import com.invex.app.ui.components.LineUnderlinedText
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.PillGrey
import com.invex.app.ui.theme.SoftWhite
import com.invex.app.ui.theme.TableHeaderText

/**
 * Header block: "InvEx" wordmark over "Already have an account ? Sign in".
 * 80dp tall: 48 wordmark + 8 gap + 24 subtext.
 */
@Composable
fun RegisterHeader(
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "InvEx",
            style = InvexType.LoginWordmark,
            color = Color.Black,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Already have an account ?",
                style = InvexType.LoginSubtext,
                color = Color.Black,
            )
            LineUnderlinedText(
                text = "Sign in",
                style = InvexType.LoginSubtext,
                color = Color.Black,
                underlineColor = Orange,
                underlineOffset = 4.dp,
                modifier = Modifier.clickable(onClick = onSignIn),
            )
        }
    }
}

/**
 * Grey 136 x 40dp pill selector ("Faculty" with an up/down arrow).
 * Tapping opens a dropdown menu to toggle between Faculty and Student.
 */
@Composable
fun RegisterRoleSelector(
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
            Text(
                text = selected.label,
                style = InvexType.RoleLabel,
                color = Color.Black,
            )
            Image(
                painter = painterResource(R.drawable.ic_unfold_more),
                contentDescription = "Choose role",
                modifier = Modifier.size(16.dp),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
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
 * Underlined form field:
 * Label with an orange underline bar, followed by a flat bottom-divider text input line.
 */
@Composable
fun RegisterField(
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
        // Label with 2dp orange underline
        LineUnderlinedText(
            text = label,
            style = InvexType.FieldLabel,
            color = Color.Black,
            underlineColor = Orange,
            underlineOffset = 4.dp,
        )

        // Flat underlined input line matching the Figma reference design
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
            )

            // Horizontal underline border
            HorizontalDivider(
                modifier = Modifier.align(Alignment.BottomCenter),
                thickness = 1.dp,
                color = TableHeaderText,
            )
        }
    }
}

/**
 * Orange full-width "Create your account" registration button.
 */
@Composable
fun RegisterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isLoading) Orange.copy(alpha = 0.6f) else Orange)
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (isLoading) "Creating..." else "Create your account",
            style = InvexType.LoginButton,
            color = SoftWhite,
        )
    }
}

/**
 * Centered legal disclaimer under the button.
 * Centered across 3 lines matching the Register screenshot.
 */
@Composable
fun RegisterTermsText(modifier: Modifier = Modifier) {
    Text(
        text = "By clicking continue, you agree to\nour Terms of Service and Privacy\nPolicy.",
        style = InvexType.Terms.copy(textAlign = TextAlign.Center),
        color = TableHeaderText,
        textAlign = TextAlign.Center,
        modifier = modifier.width(260.dp),
    )
}
