Const LETTERS_START% = 65
Const LETTERS_END% = 90
Const NUMBERS_START% = 48
Const NUMBERS_END% = 57
Const SYMBOL_START% = 58
Const SYMBOL_END% = 64
Const CHAR_SIZE% = 16

Function frame%(cell%, size%) 
	Return cell * size + 1 + cell
End Function

Global fontsheet = LoadImage("font.bmp")
MaskImage(fontsheet, 255, 0, 255)

Function drawText(x%, y%, t$, centerd)
	l% = Len(t)
	realL = Len(t)*CHAR_SIZE

	For i = 0 To l
		c$ = Mid(t, i, 1)
		cv% = Asc(c)

		If cv >= LETTERS_START And cv <= LETTERS_END Then
			s% = cv - LETTERS_START
			DrawImageRect(fontsheet, x+i*CHAR_SIZE-realL/2, y, frame(s, CHAR_SIZE), 1, CHAR_SIZE, CHAR_SIZE)
		End If 
		
		If cv >= NUMBERS_START And cv <= NUMBERS_END Then
			s% = cv - NUMBERS_START
			DrawImageRect(fontsheet, x+i*CHAR_SIZE-realL/2, y, frame(s, CHAR_SIZE), 18, CHAR_SIZE, CHAR_SIZE)
		End If 
		
		If cv >= SYMBOL_START And cv <= SYMBOL_END Then
			s% = cv - SYMBOL_START
			DrawImageRect(fontsheet, x+i*CHAR_SIZE-realL/2, y, frame(s, CHAR_SIZE), 35, CHAR_SIZE, CHAR_SIZE)
		End If 
	Next
End Function

