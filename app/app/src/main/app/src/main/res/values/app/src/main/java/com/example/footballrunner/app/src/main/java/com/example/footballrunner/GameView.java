package com.example.footballrunner;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import java.util.*;

public class GameView extends View {
    Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    Random rnd = new Random();
    float playerX, playerY, speed = 8f;
    boolean running = true, gameOver = false;
    int score = 0, coins = 0;
    long last = System.currentTimeMillis(), spawnAt = 0;
    ArrayList<Item> items = new ArrayList<>();

    static class Item {
        float x,y; int type; // 0 ball, 1 trophy, 2 obstacle
        Item(float x,float y,int type){this.x=x;this.y=y;this.type=type;}
    }

    public GameView(Context c) {
        super(c);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        setFocusable(true);
    }

    protected void onDraw(Canvas c) {
        super.onDraw(c);
        int w=getWidth(), h=getHeight();
        drawBackground(c,w,h);
        if (playerX==0) { playerX=w/2f; playerY=h*0.78f; }

        // move game
        long now=System.currentTimeMillis();
        float dt=Math.min(40, now-last)/16f; last=now;
        if (running && !gameOver) {
            score += (int)(dt*0.7f);
            if (now-spawnAt>650) {
                spawnAt=now;
                int type=rnd.nextInt(3);
                items.add(new Item(w*0.18f + rnd.nextFloat()*w*0.64f, -70, type));
            }
            for (Item it:items) it.y += speed*dt;
            for (int i=items.size()-1;i>=0;i--) {
                Item it=items.get(i);
                if (Math.abs(it.x-playerX)<75 && Math.abs(it.y-playerY)<85) {
                    if (it.type==2) gameOver=true;
                    else { coins += it.type==0?1:3; score += it.type==0?25:60; }
                    items.remove(i);
                } else if (it.y>h+100) items.remove(i);
            }
        }

        for(Item it:items) drawItem(c,it);
        drawPlayer(c,playerX,playerY);
        drawHud(c,w,h);

        if(gameOver) {
            p.setColor(0xB8000000); c.drawRect(0,0,w,h,p);
            p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(46); p.setColor(Color.WHITE);
            c.drawText("GAME OVER",w/2f,h*0.43f,p);
            p.setTextSize(24); c.drawText("Score: "+score+"   Coins: "+coins,w/2f,h*0.50f,p);
            p.setTextSize(18); c.drawText("Tap to restart",w/2f,h*0.58f,p);
        } else {
            postInvalidateDelayed(16);
        }
    }

    void drawBackground(Canvas c,int w,int h) {
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0,0,0,h,0xFF87CEEB,0xFF3E9E45,Shader.TileMode.CLAMP));
        c.drawRect(0,0,w,h,p); p.setShader(null);

        // stadium
        p.setColor(0xFF24332A); c.drawRect(0,h*0.13f,w,h*0.23f,p);
        for(int i=0;i<12;i++){p.setColor(0xFFB8D8C0); c.drawCircle(i*w/11f,h*0.18f,5,p);}

        // perspective running track
        Path path=new Path();
        path.moveTo(w*0.38f,h*0.23f); path.lineTo(w*0.62f,h*0.23f);
        path.lineTo(w*0.92f,h); path.lineTo(w*0.08f,h); path.close();
        p.setColor(0xFF287A35); c.drawPath(path,p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(4); p.setColor(0xFFE6D35A);
        c.drawLine(w*0.38f,h*0.23f,w*0.08f,h,p); c.drawLine(w*0.62f,h*0.23f,w*0.92f,h,p);
        p.setStrokeWidth(3); p.setColor(0xAAFFFFFF);
        for(int i=0;i<12;i++){
            float y=h*0.27f+i*h*0.065f;
            float center=w/2f; float half=8+i*8;
            c.drawLine(center-half,y,center+half,y,p);
        }
        p.setStyle(Paint.Style.FILL);
    }

    void drawHud(Canvas c,int w,int h){
        p.setTextAlign(Paint.Align.LEFT); p.setTextSize(24); p.setColor(Color.WHITE);
        c.drawText("⚽ "+score,24,42,p);
        p.setTextAlign(Paint.Align.RIGHT); c.drawText("🪙 "+coins,w-24,42,p);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(14); c.drawText("SWIPE / TAP LEFT OR RIGHT TO MOVE",w/2f,h-24,p);
        p.setColor(0xAA000000); c.drawRoundRect(w/2f-34,60,w/2f+34,118,22,22,p);
        p.setColor(Color.WHITE); p.setTextSize(28);
        c.drawText(running?"Ⅱ":"▶",w/2f,98,p);
    }

    void drawItem(Canvas c,Item it){
        p.setTextAlign(Paint.Align.CENTER);
        if(it.type==0){
            p.setColor(Color.WHITE); c.drawCircle(it.x,it.y,28,p);
            p.setColor(Color.BLACK); p.setTextSize(28); c.drawText("⚽",it.x,it.y+10,p);
        } else if(it.type==1){
            p.setColor(0xFFFFD54A); p.setTextSize(44); c.drawText("🏆",it.x,it.y+15,p);
        } else {
            p.setColor(0xFF151515); c.drawRoundRect(it.x-32,it.y-32,it.x+32,it.y+32,10,10,p);
            p.setColor(Color.WHITE); p.setTextSize(28); c.drawText("?",it.x,it.y+10,p);
        }
    }

    void drawPlayer(Canvas c,float x,float y){
        // stylized football runner; no real person likeness
        p.setStrokeWidth(9); p.setStrokeCap(Paint.Cap.ROUND); p.setColor(0xFF20252A);
        c.drawLine(x,y+55,x-28,y+125,p); c.drawLine(x,y+55,x+35,y+118,p);
        p.setStrokeWidth(12); p.setColor(0xFF1685D8);
        c.drawLine(x,y+5,x,y+58,p); c.drawLine(x,y+15,x-38,y+55,p); c.drawLine(x,y+18,x+42,y-5,p);
        p.setColor(0xFFB87950); c.drawCircle(x,y-18,25,p);
        p.setColor(0xFF242424); c.drawArc(x-25,y-43,x+25,y-2,180,180,true,p);
        p.setColor(Color.WHITE); p.setTextAlign(Paint.Align.CENTER); p.setTextSize(12);
        c.drawText("9",x,y+35,p);
    }

    public boolean onTouchEvent(MotionEvent e){
        if(e.getAction()==MotionEvent.ACTION_DOWN){
            if(gameOver){ reset(); return true; }
            float x=e.getX();
            if (x<getWidth()/2f) playerX-=getWidth()*0.18f;
            else playerX+=getWidth()*0.18f;
            playerX=Math.max(getWidth()*0.13f,Math.min(getWidth()*0.87f,playerX));
            if(e.getY()<140 && Math.abs(x-getWidth()/2f)<100) running=!running;
            return true;
        }
        if(e.getAction()==MotionEvent.ACTION_MOVE){
            playerX=e.getX();
            playerX=Math.max(getWidth()*0.13f,Math.min(getWidth()*0.87f,playerX));
            return true;
        }
        return true;
    }

    void reset(){
        score=0; coins=0; items.clear(); gameOver=false; running=true;
        playerX=getWidth()/2f; last=System.currentTimeMillis(); spawnAt=0;
        invalidate();
    }
}
