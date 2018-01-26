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
	
	Field friction#
	
	Field typeOf
	
	Field size#
	
	Field wobble#
	Field wobbleCount#
	
	Field r%, g%, b%
	
	Field destroy
End Type

Function growPlayer(typeOf2, amount%)
	For p.player = Each player
		If p\typeOf = typeOf2 Then p\size = p\size + amount
	Next
End Function

Function addPlayer(x2#, y2#, typeOf2)
	p.player = New player
	p\x = x2
	p\y = y2
	
	p\typeOf = typeOf2
	
	p\size = SPRITE_SIZE
	
	p\friction = 0.9
	p\speed = 4
	
	p\r = Rand(100, 255)
	p\g = Rand(100, 255)
	p\b = Rand(100, 255)
End Function 

Function updatePlayer()
	For p.player = Each player
		p\x = p\x + p\velX
		p\y = p\y + p\velY
		
		p\velX = p\velX * p\friction
		p\velY = p\velY * p\friction
		
		p\wobbleCount = p\wobbleCount + 1
		p\wobble = Cos(p\wobbleCount*8)
		
		If JoyY(p\typeOf) >= JOY_THRESHOLD Or JoyY(p\typeOf) <= -JOY_THRESHOLD Or JoyX(p\typeOf) >= JOY_THRESHOLD Or JoyX(p\typeOf) <= -JOY_THRESHOLD Then 
			p\angle = ATan2(JoyY(p\typeOf), JoyX(p\typeOf))
			
			p\velX = (p\speed * Cos(p\angle))
			p\velY = (p\speed * Sin(p\angle))
		End If
		
		p\shootAngle = ATan2(JoyYaw(p\typeOf), JoyPitch(p\typeOf))
		Text 0, 32*p\typeOf, p\shootAngle
		
		If JoyZ(p\typeOf) <= -0.9 Then
			addProjectile(p\x, p\y, p\shootAngle, 4, 4, p\typeOf)
		End If

		If p\typeOf Then
			
		Else
			
		End If 
	Next
End Function

Function drawPlayer()
	For p.player = Each player
		Plot p\x, p\y
		Color p\r, p\g, p\b
		Rect p\x-p\size/2, p\y-p\size/2, p\size+p\wobble, p\size+p\wobble
		Color 255, 255, 255
	Next
End Function

Type projectile
	Field x#
	Field y#
	
	Field angle#
	Field speed#
	
	Field size%
	Field r%,g%,b%
	
	Field tag%
	
	Field destroy
End Type

Function addProjectile(x2#, y2#, angle2#, speed2#, size2#, tag2%)
	p.projectile = New projectile
	p\x = x2
	p\y = y2
	p\angle = angle2
	p\speed = speed2
	p\size = size2
	p\tag = tag2
	
	p\r = Rand(100, 255)
	p\g = Rand(100, 255)
	p\b = Rand(100, 255)
End Function

Function updateProjectile()
	For p.projectile = Each projectile
		p\x = p\x + (p\speed * Cos(p\angle))
		p\y = p\y + (p\speed * Sin(p\angle))
	Next
End Function

Function drawProjectile()
	For p.projectile = Each projectile
		Color p\r, p\g, p\b
		drawLine(p\x, p\y, p\x + Cos(p\angle) * p\size, p\y + Sin(p\angle) * p\size, 2, 0)
		Color 255, 255, 255
	Next
End Function

Type loot
	Field x%
	Field y%
	
	Field life%
	
	Field destroy
End Type 

Function updateLoot()
	For l.loot = Each loot
	
	Next
End Function

Function drawLoot()
	For l.loot = Each loot
	
	Next
End Function

Function update()
	updatePlayer()
	updateProjectile()
	updateLoot()
End Function 

Function draw()
	drawPlayer()
	drawProjectile()
	drawLoot()
End Function

addPlayer(100, 200, 0)
addPlayer(300, 300, 1)

While Not KeyHit(1)
	Cls 
	update()
	draw()
	Flip
Wend