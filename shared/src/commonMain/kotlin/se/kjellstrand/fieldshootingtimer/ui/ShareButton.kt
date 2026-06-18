package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import se.kjellstrand.fieldshootingtimer.resources.Res
import se.kjellstrand.fieldshootingtimer.resources.share
import se.kjellstrand.fieldshootingtimer.resources.share_app
import se.kjellstrand.fieldshootingtimer.ui.theme.BlackColor

@Composable
fun ShareButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(Res.drawable.share),
            contentDescription = stringResource(Res.string.share_app),
            tint = BlackColor
        )
    }
}
