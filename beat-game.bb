Const DEBUG = 2

Const WIN_W = 640, WIN_H = 480
Graphics WIN_W, WIN_H, 8, DEBUG

SeedRnd MilliSecs()

Global frametimer=CreateTimer(60)
Global starttime=MilliSecs(),elapsedtime,fpscounter,curfps

Const LEFT_KEY = 203
Const RIGHT_KEY = 205
Const UP_KEY = 200
Const DOWN_KEY = 208

Const A_KEY = 30
Const D_KEY = 32
Const S_KEY = 31
Const W_KEY = 17

Global correct = LoadSound("correct.wav")
Global wrong = LoadSound("wrong.wav")
Global error = LoadSound("error.wav")

Function collision(x, y, w, h, x2, y2, w2, h2)
	If y >= y2 + h2 Then Return False 
	If x >= x2 + w2 Then Return False 
	If y + h <= y2 Then Return False
	If x + w <= x2 Then Return False   
	Return True 
End Function

Function clamp#(v#, min#, max#)
	If v < min Then Return min
	If v > max Then Return max
	Return v
End Function 

Function distanceTo#(x, y, x2, y2)
	Return Sqr((x-x2)^2 + (y-y2)^2)
End Function 

Function frame%(cell%, size%) 
	Return cell * size + 1 + cell
End Function

Function drawLine(sx, sy, ex, ey, w%, interval%)
	Local a# = ATan2(ey-sy, ex-sx)
	Local d = distanceTo(sx, sy, ex, ey)
	
	For i = 0 To (d/w) 
		If w = 1 Then 
		 	If interval <> 0 Then 
				If i Mod interval = 0 Then Plot sx + Cos(a)*i, sy + Sin(a)*i
			Else
				Plot sx + Cos(a)*i, sy + Sin(a)*i
			End If
		Else
			If interval <> 0 Then 
				If i Mod interval = 0 Then Rect sx + Cos(a)*(i*w), sy + Sin(a)*(i*w), w, w
			Else
				Rect sx + Cos(a)*(i*w), sy + Sin(a)*(i*w), w, w
			End If
		End If
	Next
End Function

Function lineCollision(sx%, sy%, ex%, ey%, w%, x%, y%, width%, height%)
	Local a# = ATan2(ey-sy, ex-sx)
	Local d = distanceTo(sx, sy, ex, ey)
	
	For i = 0 To (d/w)-1 
		If w = 1 Then 
			If collision(sx + Cos(a)*i, sy + Sin(a)*i, 1, 1, x, y, width, height) Then Return 1
		Else
			If collision(sx + Cos(a)*(i*w), sy + Sin(a)*(i*w), w, w, x, y, width, height) Then Return 1
		End If
	Next
	
	Return 0
End Function

Function lerp#(x#, y#, t#)
	Return t# * y# + (1-t#) * x#
End Function

Global time# = 1
Const SLOW_TIME# = 0.3

Const SIZE = 32

Function randomChar$()
	Local t$ = Str(Rand(0, 1))
	r = Rand(0, 10)
	If r = 5 Then t = Chr(Rand(65, 75))
	Return t
End Function

Function generateString$()
	Local t$
	
	For i = 1 To SIZE 
		t = t + randomChar()
	Next 
	
	Return t
End Function

Type player
	Field x%, y%

	Field score%
	Field tag%
	
	Field hasPressed
	Field correctPressed
	
	Field orginalBar#
	Field censorBar#
	Field targetCensorBar#
	
	Field d$
End Type

Function addPlayer(x2, y2, d2$, tag2%)
	p.player = New player
	p\x = x2
	p\y = y2
	
	p\d = d2
	
	p\tag = tag2
	
	p\orginalBar = WIN_H - p\y
	p\targetCensorBar = p\orginalBar
	p\censorBar = p\targetCensorBar
End Function

Function getTopChar$(t$)
	Return Mid(t, 1, 1)
End Function

Function removeTopChar$(t$)
	Return Mid(t, 2, Len(t)-1)
End Function

Global playerWhoWon = -1

Const JOY_LIMIT# = 0.8

Function updatePlayer()
	For p.player = Each player
		p\censorBar = lerp(p\censorBar, p\targetCensorBar, 0.1)
	
		If Len(p\d) <= 0 Then playerWhoWon = p\tag
	
		If JoyZ(p\tag) <= 0.1 And JoyZ(p\tag) >= -0.1  And JoyDown(3, p\tag) = 0 Then
			p\hasPressed = 0
		End If
	
		If JoyZ(p\tag) >= JOY_LIMIT And p\hasPressed = 0 Then
			If getTopChar(p\d) = "0" Then
				p\score = p\score + 1
				p\correctPressed = 1
			Else
				p\score = p\score - 1
				p\d = p\d + randomChar()
				p\targetCensorBar = p\orginalBar
				PlaySound error
			End If
			p\hasPressed = 1
		End If 
		
		If JoyZ(p\tag) <= -JOY_LIMIT And p\hasPressed = 0  Then
			If getTopChar(p\d) = "1" Then
				p\score = p\score + 1
				p\correctPressed = 1
			Else
				p\score = p\score - 1
				p\d = p\d + randomChar()
				p\targetCensorBar = p\orginalBar
				PlaySound error
			End If
			p\hasPressed = 1
		End If
		
		If JoyHit(3, p\tag) And p\hasPressed = 0 Then
			If getTopChar(p\d) <> "0" And getTopChar(p\d) <> "1" Then
				p\score = p\score + 2
				p\correctPressed = 1
			Else
				p\score = p\score - 1
				p\d = p\d + randomChar()
				p\targetCensorBar = p\orginalBar
				PlaySound error
			End If

			p\hasPressed = 1
		End If
		
		If p\correctPressed Then
			If getTopChar(p\d) = "0" Or getTopChar(p\d) = "1" Then
				addParticle(p\x, p\y+13*1, 0, 0, 0, 255, 0, getTopChar(p\d), 0)
				PlaySound correct
			Else
				addParticle(p\x, p\y+13*1, -45+Rnd(-16, 16), Rnd(4, 8), 255, 0, 0, getTopChar(p\d), 1)
				PlaySound wrong
			End If
			p\d = removeTopChar(p\d)
			p\targetCensorBar = p\targetCensorBar - 10
			p\correctPressed = 0
		End If
	Next
End Function 

Function drawPlayer()
	For p.player = Each player
		For i = 1 To Len(p\d)
			c$ = Mid(p\d, i, 1)
			If c = "0" Or c = "1" Then 
				Color 0, 255, 0
			Else
				Color 255, 0, 0
			End If
			Text p\x, p\y+13*i, c
			Color 255, 255, 255
		Next
		Plot p\x, p\y+13*2
		Text p\x-64, p\y-100, "P" + (p\tag+1) + "Score: " + p\score
		Color 255, 255, 255
		Line p\x, p\y+13*2, p\x+100, p\y+13*2
		Color 0, 0, 0
		Rect p\x, p\y+13*2+2 + (p\orginalBar - p\censorBar), 100, p\censorBar
		Color 255, 255, 255
	Next
End Function 

Type particle
	Field x#
	Field y#
	
	Field angle#
	Field speed#
	Field acceleration#
	
	Field l$
	
	Field r#, g#, b#
	
	Field tag%
	
	Field destroy
End Type

Function addParticle(x2#, y2#, angle2#, speed2#, r2#, g2#, b2#, l2$, tag2%)
	p.particle = New particle
	p\x = x2
	p\y = y2
	
	p\angle = angle2
	p\speed = speed2
	
	p\r = r2
	p\g = g2
	p\b = b2
	
	p\l = l2
	
	p\tag = tag2
End Function

Function updateParticle()
	For p.particle = Each particle
		If p\tag = 0 Then
			p\y = lerp(p\y, -15, 0.05)
			p\r = lerp(p\r, 0, 0.05)
			p\g = lerp(p\g, 0, 0.05)
			p\b = lerp(p\b, 0, 0.05)
		Else If p\tag = 1
			p\x = p\x + (p\speed * Cos(p\angle))
			p\y = p\y + (p\speed * Sin(p\angle))
			
			p\y = p\y + p\acceleration
			p\acceleration = p\acceleration + 1
			
			If p\y + p\acceleration + 11 >= WIN_H Then
				p\acceleration = -p\acceleration/4
			End If
		End If
		
		If p\y <= -11 Then p\destroy = 1
		If p\y >= WIN_H Then p\destroy = 1
		If p\x >= WIN_W Then p\destroy = 1
		
		If p\destroy Then Delete p 
	Next
End Function

Function drawParticle()
	For p.particle = Each particle
		Color p\r, p\g, p\b
		Text p\x, p\y, p\l
		Color 255, 255, 255
	Next
End Function

Global startScreen = 1

Function update()
	If startScreen Then
		If JoyDown(8, 0) Or JoyDown(8, 1) Then
			restartGame()
			startScreen = 0
		End If
	Else
		updateParticle()
		If playerWhoWon = -1 Then
			updatePlayer()
		Else
			If JoyDown(8, 0) Or JoyDown(8, 1) Then
				restartGame()
			End If
		End If 
	End If
End Function

Function draw()
	drawPlayer()
	drawParticle()
	
	If playerWhoWon >= 0 Then
		Color Rand(50, 255), Rand(50, 255), Rand(50, 255)
		Text WIN_W/2, WIN_H/2, "PLAYER " + (playerWhoWon+1) + " WON!", 1, 1
		Text WIN_W/2, WIN_H/2+32, "PRESS START TO RESTART", 1, 1
		Color 255, 255, 255
	End If 
	
	If startScreen Then
		Color Rand(50, 255), Rand(50, 255), Rand(50, 255)
		Text WIN_W/2, WIN_H/2, "BYTE PACKER", 1, 1
		Text WIN_W/2, WIN_H/2+32, "PRESS START TO START", 1, 1
		Color 255, 255, 255
	End If
	
	Text WIN_W-200, 10, "Left-trigger for 0-bits"
	Text WIN_W-200, 25, "Right-trigger for 1-bits"
	Text WIN_W-200, 40, "X button for all others"
End Function

HidePointer

Function restartGame()
	For p.player = Each player
		Delete p
	Next
	
	For pa.particle = Each particle
		Delete pa
	Next
	
	playerWhoWon = -1
	
	addPlayer(100, 100, generateString(), 0)
	addPlayer(300, 100, generateString(), 1)
End Function

While Not KeyHit(1)
	Cls 
	
	update()
	draw()

	Flip
Wend

