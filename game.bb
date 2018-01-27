Const WIN_W = 640, WIN_H = 480
Graphics WIN_W, WIN_H, 8, 2

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

Const JOY_THRESHOLD# = 0.4

Const SPRITE_SIZE = 16

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

Function drawTriangle(x%, y%, s%, angle#)
	Local r# = 120

	Local p1x# = x + Cos(angle) * s
	Local p1y# = y + Sin(angle) * s
		
	Local p2x# = x + Cos(angle + r) * s
	Local p2y# = y + Sin(angle + r) * s
		
	Local p3x# = x + Cos(angle - r) * s
	Local p3y# = y + Sin(angle - r) * s
	
	Line p1x, p1y, p2x, p2y
	Line p2x, p2y, p3x, p3y
	Line p1x, p1y, p3x, p3y
	Color 255, 255, 255
End Function

Global time# = 1
Const SLOW_TIME# = 0.3

Type player
	Field x#
	Field y#
	
	Field velX#
	Field velY#
	Field angle#
	Field shootAngle#
	Field speed#
	
	Field crosshairX#
	Field crosshairY#
	
	Field friction#
	
	Field fireRate%
	Field maxFireRate%
	Field fireRateBar#
	
	Field buildCount%
	Field maxBuildCount%
	Field buildCountBar#
	
	Field typeOf
	
	Field size#
	Field targetSize#
	
	Field wobble#
	Field wobbleCount#
	Field amp#
	
	Field r#, g#, b#
	Field orignalR%, orignalG%, orignalB%
	
	Field hitCount%
	
	Field destroy
End Type

Function growPlayer(typeOf2, amount%)
	For p.player = Each player
		If p\typeOf = typeOf2 Then p\targetSize = p\targetSize + amount
	Next
End Function

Function addPlayer(x2#, y2#, typeOf2)
	p.player = New player
	p\x = x2
	p\y = y2
	
	p\typeOf = typeOf2
	
	p\size = SPRITE_SIZE
	p\targetSize = p\size
	
	p\friction = 0.9
	p\speed = 4
	
	p\amp = 1
	
	p\maxFireRate = 64
	p\maxbuildCount = 64
	If p\typeOf Then p\maxFireRate = 32*3
	
	p\r = Rand(100, 255)
	p\g = Rand(100, 255)
	p\b = Rand(100, 255)
	p\orignalR = p\r
	p\orignalG = p\g
	p\orignalB = p\b
End Function 

Function updatePlayer()
	For p.player = Each player
		p\x = p\x + p\velX
		p\y = p\y + p\velY
		
		p\velX = p\velX * p\friction
		p\velY = p\velY * p\friction
		
		p\amp = lerp(p\amp, 1, 0.1)
		p\wobbleCount = p\wobbleCount + 1
		p\wobble = p\amp * Cos(p\wobbleCount*8)
		
		If JoyY(p\typeOf) >= JOY_THRESHOLD Or JoyY(p\typeOf) <= -JOY_THRESHOLD Or JoyX(p\typeOf) >= JOY_THRESHOLD Or JoyX(p\typeOf) <= -JOY_THRESHOLD Then 
			p\angle = ATan2(JoyY(p\typeOf), JoyX(p\typeOf))
			
			p\velX = (p\speed * Cos(p\angle))
			p\velY = (p\speed * Sin(p\angle))
		End If
		p\crosshairX = lerp(p\crosshairX, (Cos(p\shootAngle) * p\size), 0.3)
		p\crosshairY = lerp(p\crossHairY, (Sin(p\shootAngle) * p\size), 0.3)
		If Sqr(JoyYaw(p\typeOf)^2 + JoyPitch(p\typeOf)^2) >= 50 Then p\shootAngle = ATan2(JoyYaw(p\typeOf), JoyPitch(p\typeOf))
		p\fireRateBar = lerp(p\fireRateBar, (Float(p\fireRate)/Float(p\maxFireRate))*p\size, 0.3)
		If JoyZ(p\typeOf) <= -0.9 Then
			If p\typeOf Then
				p\fireRate = p\fireRate + 1
				
				If p\fireRate >= p\maxFireRate Then 
					addProjectile(p\x, p\y, p\shootAngle, 3+Abs(p\velX), 8*2, 4, p\typeOf)
					p\fireRate = 0
				End If
			Else 
				p\fireRate = p\fireRate + 1
				
				If p\fireRate = p\maxFireRate Or p\fireRate = p\maxFireRate-8 Or p\fireRate = p\maxFireRate-8*2 Then
					addProjectile(p\x, p\y, p\shootAngle, 5+Abs(p\velX), 4, 2, p\typeOf)
				End If
			
				If p\fireRate >= p\maxFireRate Then p\fireRate = 0
			End If
		Else
			p\fireRate = 0
		End If 
		
		p\size = lerp(p\size, p\targetSize, 0.1)
 		
		If p\hitCount >= 1 Then
			p\hitCount = p\hitCount + 1
			If p\hitCount >= 32 Then p\hitCount = 0
			p\r = lerp(p\r, 255, 0.2)
			p\g = lerp(p\g, 0, 0.4)
			p\b = lerp(p\b, 0, 0.4)
		Else
			p\r = lerp(p\r, p\orignalR, 0.2)
			p\g = lerp(p\g, p\orignalG, 0.2)
			p\b = lerp(p\b, p\orignalB, 0.2)
		End If 

		If p\typeOf Then
			
		Else
			p\buildCountBar = lerp(p\buildCountBar, (Float(p\buildCount)/Float(p\maxBuildCount))*p\size, 0.3)
			
			Local canBuild
			
			For l.loot = Each loot
				canBuild = l\builderIsNear
				
				If p\buildCount >= p\maxbuildCount Then
					l\healedCount = 1
					p\buildCount = 0
					
					growPlayer(1, 1)
					growPlayer(0, 1)
				End If
			Next

			If canBuild Then
				If JoyDown(1) Then 
					p\buildCount = p\buildCount + 1
				Else
					p\buildCount = 0
				End If
			Else
				p\buildCount = 0
			End If
		End If 
	Next
End Function

Function drawPlayer()
	For p.player = Each player
		Plot p\x, p\y
		Color p\r, p\g, p\b
		Rect p\x-p\size/2-p\wobble/2, p\y-p\size/2-p\wobble/2, p\size+p\wobble, p\size+p\wobble
		Color p\fireRate, 255-p\fireRate*2, 0
		Rect p\x-p\size/2, p\y-p\size/2-8, p\fireRateBar, 4
		Color 255, 255, 255
		Rect (p\x-p\size/2)-8, p\y-p\size/2, 4, p\buildCountBar
		drawTriangle(p\x + p\crosshairX, (p\y-1) + p\crosshairY, p\size/2, p\shootAngle)
	Next
End Function

Type projectile
	Field x#
	Field y#
	
	Field angle#
	Field speed#
	
	Field size%
	Field width%
	Field r%,g%,b%
	
	Field tag%
	
	Field destroy
End Type

Function addProjectile(x2#, y2#, angle2#, speed2#, size2#, width2%, tag2%)
	p.projectile = New projectile
	p\x = x2
	p\y = y2
	p\angle = angle2
	p\speed = speed2
	p\size = size2
	p\width = width2
	p\tag = tag2
	
	p\r = Rand(100, 255)
	p\g = Rand(100, 255)
	p\b = Rand(100, 255)
End Function

Function updateProjectile()
	For p.projectile = Each projectile
		p\x = p\x + (p\speed * Cos(p\angle))
		p\y = p\y + (p\speed * Sin(p\angle))
		
		For pl.player = Each player
			If p\tag <> pl\typeOf And lineCollision(p\x, p\y, p\x + Cos(p\angle) * p\size, p\y + Sin(p\angle) * p\size, 2, pl\x-pl\size/2, pl\y-pl\size/2, pl\size+pl\wobble, pl\size+pl\wobble) Then
				If pl\typeOf Then
					growPlayer(1, -1)
					growPlayer(0, 1)
				Else
					growPlayer(0, -3)
				End If
				pl\hitCount = 1
				pl\amp = 8
				p\destroy = 1
			End If 
		Next
		
		If p\destroy Then Delete p
	Next
End Function

Function drawProjectile()
	For p.projectile = Each projectile
		Color p\r, p\g, p\b
		drawLine(p\x, p\y, p\x + Cos(p\angle) * p\size, p\y + Sin(p\angle) * p\size, p\width, 0)
		Color 255, 255, 255
	Next
End Function

Type loot
	Field x%
	Field y%
	
	Field life%
	Field maxLife%
	Field lifeSize#
	Field lifeTime
	
	Field hitCount%
	Field healedCount%
	
	Field builderIsNear

	Field size#
	Field r#, g#, b#
	Field orginalR%, orginalG%, orginalB%
	
	Field destroy
End Type 

Function addLoot(x2%, y2%)
	l.loot = New loot
	l\x = x2
	l\y = y2
	
	l\size = SPRITE_SIZE*2
	
	l\maxLife = 5
	l\life = l\maxLife
	l\lifeTime = 128*12
	
	l\r = Rand(100, 255)
	l\g = Rand(100, 255)
	l\b = Rand(100, 255)
	
	l\orginalR = l\r
	l\orginalG = l\g
	l\orginalB = l\b
End Function 

Function updateLoot()
	For l.loot = Each loot
		l\lifeTime = l\lifeTime - 1
		If l\lifeTime <= 32 Then 
			l\r = Rand(255)
			l\g = Rand(255)
			l\b = Rand(255)
		End If
		If l\lifeTime <= 0 Or l\life < 0 Then
			l\size = l\size - 1
			l\lifeSize = l\lifeSize - 1
			l\builderIsNear = 0
			
			If l\size < 1 Then l\destroy = 1
		Else
			l\lifeSize = lerp(l\lifeSize, (Float(l\life)/Float(l\maxLife))*(l\size-4), 0.1)
		End If
		
		If l\hitCount >= 1 Then
			l\hitCount = l\hitCount + 1
			l\r = lerp(l\r, 255, 0.1)
			l\g = lerp(l\g, 0, 0.1)
			l\b = lerp(l\b, 0, 0.1)
			
			If l\hitCount >= 32 Then
				l\hitCount = 0
			End If
		Else 
			If l\healedCount > 0 Then 
				If l\healedCount = 1 Then l\life = l\life + 1
				l\healedCount = l\healedCount + 1
				
				If l\healedCount >= 16 Then l\healedCount = 0
				
				l\r = lerp(l\r, 0, 0.1)
				l\g = lerp(l\g, 255, 0.1)
				l\b = lerp(l\b, 0, 0.1)
			Else
				l\r = lerp(l\r, l\orginalR, 0.1)
				l\g = lerp(l\g, l\orginalG, 0.1)
				l\b = lerp(l\b, l\orginalB, 0.1)
			End If
		End If
		
		If l\life <> l\maxLife Then 
			For pl.player = Each player
				If pl\typeOf = 0 Then
					l\builderIsNear = (distanceTo(pl\x, pl\y, l\x, l\y) <= SPRITE_SIZE * 8)
				End If
			Next
		Else
			l\builderIsNear = 0
		End If 
		
		For p.projectile = Each projectile
			If lineCollision(p\x, p\y, p\x + Cos(p\angle) * p\size, p\y + Sin(p\angle) * p\size, 2, l\x-l\size/2, l\y-l\size/2, l\size, l\size) Then
				If p\tag Then 
					l\life = l\life - 1
					l\hitCount = 1
				End If 
				p\destroy = 1
			End If
		Next
		
		If l\destroy Then Delete l
	Next
End Function

Function drawLoot()
	For l.loot = Each loot
		Rect l\x-l\size/2, l\y-l\size/2, l\size, l\size, 0
		Oval (l\x+1)-(l\size-2)/2, (l\y+1)-(l\size-2)/2, l\size-2, l\size-2, 0
		Color l\r, l\g, l\b
		Oval (l\x+1)-l\lifeSize/2, (l\y+1)-l\lifeSize/2, l\lifeSize, l\lifeSize, 1
		Color 255, 255, 255
	
		If l\builderIsNear Then
			For p.player = Each player
				If p\typeOf = 0 Then 
					Line p\x, p\y, l\x, l\y
					Oval p\x-2, p\y-2, 4, 4
					Oval l\x-2, l\y-2, 4, 4		
				End If
			Next
		End If
	Next
End Function

Function restartGame()

End Function

Function updateLevel()

End Function

Function generateLevel()

End Function

Function update()
	updatePlayer()
	updateProjectile()
	updateLoot()
	
	updateLevel()
End Function 

Function draw()
	drawPlayer()
	drawProjectile()
	drawLoot()
End Function

addPlayer(100, 200, 0)
addPlayer(300, 300, 1)

addLoot(200, 200)

HidePointer

While Not KeyHit(1)
	Cls 
	update()
	draw()
	Flip
Wend