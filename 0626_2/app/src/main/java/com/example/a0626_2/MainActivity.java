package com.example.a0626_2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    static final int Bomb    	= -100;	//	爆弾を示す定数
    static final int Hiden0 	=  -50;	//	隠された0を示す定数
    static final int SZ			=   90;	//	セルサイズ
    public mineSweeper mS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout LL=new LinearLayout(this);
        LL.setOrientation(LinearLayout.HORIZONTAL);
        setContentView(LL);
        mS= new mineSweeper(this,10,10,10);
        LL.addView(mS);
    }
    class mineSweeper extends View {
        int numX, numY,numB;		// X方向セル数，Y方向セル数, 爆弾数
        int[][]DT;					// セルのデータ
        int AX, AY;					// 失敗時の爆弾の位置。AX=0のとき成功
        boolean exeMode=true;
        Paint pString  = new Paint();//文字表示用
        Paint pBlack   = new Paint();//線表示用   黒色
        Paint pBlack2  = new Paint();//           黒色(太線）
        Paint pGray    = new Paint();//           灰色
        Paint pLGray   = new Paint();//           明るい灰色
        Paint pWhite   = new Paint();//           白色
        Paint bGreen   = new Paint();//塗り潰し用 緑色
        Paint bRed     = new Paint();//           赤色
        Paint bYellow  = new Paint();//           黄色
        Paint bLYellow = new Paint();//           明るい黄色
        Paint bLGray   = new Paint();//           明るい灰色
        Paint bBlack   = new Paint();//           黒色
        public mineSweeper(Context context, int NX, int NY, int NB) {
            super(context);
            numX=NX;numY=NY; numB=NB;
            DT=new int[numX+2][numY+2];
            pBlack.setColor(Color.BLACK)   ; pBlack.setStyle(Paint.Style.STROKE);
            pGray.setColor(Color.GRAY)     ; pGray.setStyle(Paint.Style.STROKE);
            pLGray.setColor(0xFFDDDDDD)    ; pLGray.setStyle(Paint.Style.STROKE);
            pWhite.setColor(Color.WHITE)   ; pWhite.setStyle(Paint.Style.STROKE);
            bRed.setColor(Color.RED)       ; bRed.setStyle(Paint.Style.FILL);
            bGreen.setColor(0XFF80FF80)    ; bGreen.setStyle(Paint.Style.FILL);
            bYellow.setColor(Color.YELLOW) ; bYellow.setStyle(Paint.Style.FILL);
            bLYellow.setColor(0XFFFFFF80)  ; bLYellow.setStyle(Paint.Style.FILL);
            bBlack.setColor(Color.BLACK)   ; bBlack.setStyle(Paint.Style.FILL);
            bLGray.setColor(0xFFDDDDDD)    ; bLGray.setStyle(Paint.Style.FILL);
            // 太線用（爆弾描画用）
            pBlack2.setColor(Color.BLACK)  ; pBlack2.setStyle(Paint.Style.STROKE);
            pBlack2.setStrokeWidth(2F);
            // 文字表示用
            pString.setColor(Color.BLACK)  ; pString.setStyle(Paint.Style.FILL);
            pString.setTextSize(60);
            Initialize(); this.invalidate();
        }
        private int countB(int count, int X, int Y){// 爆弾の個数カウント
            if (DT[X][Y] == Bomb) return count + 1; else return count;
        }
        private void setBomb(Random rn){			//  爆弾の生成
            int X, Y;
            do{ X = rn.nextInt(numX) + 1; Y = rn.nextInt(numY) + 1;}
            while (DT[X][Y] == Bomb);
            DT[X][Y] = Bomb;
        }
        private void countSetNum(int X, int Y){	//爆弾の数をカウントしてセットする。
            int count = 0;
            for(int i=X-1;i<=X+1;i++)for(int j=Y-1;j<=Y+1;j++)
                if(i!=X || j!=Y) count = countB(count, i,j);
            if (count > 0) DT[X][Y] = -count;
        }
        private void Initialize(){ 						// 初期化
            exeMode = true; Random rn = new Random();
            for (int X = 0; X < numX + 2; X++)           //  初期値は隠された0とする
                for (int Y = 0; Y < numY + 2; Y++) DT[X][Y] = Hiden0;
            for (int i = 0; i < numB; i++) setBomb(rn); //  爆弾の生成
            for (int X = 1; X <= numX; X++)             //  周囲の爆弾の個数カウント
                for (int Y = 1; Y <= numY; Y++)
                    if (DT[X][Y] != Bomb) countSetNum(X, Y);
        }
        public boolean onTouchEvent(MotionEvent e){//タッチされたとき
            if(e.getAction()==MotionEvent.ACTION_DOWN){
                if(exeMode)cellSelect((int)(e.getX())/SZ,(int)(e.getY())/SZ);
                else Initialize();
                this.invalidate();
            }
            return true;
        }
        private void cellSelect(int X, int Y){		// セルが選択されたときの処理
            if (X < 1 || X > numX || Y < 1 || Y > numY) return;
            if (DT[X][Y] == 0) return;
            if (DT[X][Y] == Bomb){ // 爆弾位置が選択されたとき
                AX = X; AY = Y; exeMode = false;
                this.invalidate();
                Toast.makeText(getApplicationContext(),"残念！そこは爆弾です",
                        Toast.LENGTH_SHORT).show();
            }
            else {                // 爆弾位置でないセルが選択されたとき
                if       (DT[X][Y] == Hiden0 ) { DT[X][Y] = 0; sweep(); }
                else if  (DT[X][Y]<0         )   DT[X][Y] = -DT[X][Y];
                exeMode = continueCheck();
                if (!exeMode) { AX = 0;
                    Toast.makeText(getApplicationContext(),"ゲームクリア！",
                            Toast.LENGTH_SHORT).show();;
                }
            }
        }
        private Boolean continueCheck(){ 	// 継続チェック（終了のときfalse, 継続のときtrue)
            int X, Y;
            for (X = 1; X <= numX; X++) for (Y = 1; Y <= numY; Y++)
                if (DT[X][Y] < 0 && DT[X][Y] != Bomb) return true;
            return false;
        }
        private Boolean openCheck(Boolean iflag, int X, int Y) {	//  隠された0を開かれた0にする
            if     (DT[X][Y] == Hiden0) {DT[X][Y] = 0; return true;}// 開かれた0にしたらtrue
            else if(DT[X][Y] != Bomb  ) {
                if(DT[X][Y]<0) DT[X][Y] = -DT[X][Y];// 閉じている数を開く(負のとき閉じている）
            }
            return iflag;
        }
        private Boolean UpDownLeftRight(Boolean iflag, int X, int Y){ // 上下左右を検査
            iflag = openCheck(iflag, X-1, Y);
            iflag = openCheck(iflag, X+1, Y);
            iflag = openCheck(iflag, X, Y-1);
            iflag = openCheck(iflag, X, Y+1);
            return iflag;
        }
        private void sweep(){									//開くかどうかを判定
            Boolean iflag = true;int X,Y;
            while (iflag){
                iflag = false;
                for (X = 1; X <= numX; X++) for (Y = 1; Y <= numY; Y++)
                    if (DT[X][Y] == 0) iflag = UpDownLeftRight(iflag, X, Y);
                for (Y = numY; Y >= 1; Y--) for (X = 1; X <= numX; X++)
                    if (DT[X][Y] == 0) iflag = UpDownLeftRight(iflag, X, Y);
                for (X = numX; X >=1; X--) for (Y = 1; Y <= numY; Y++)
                    if (DT[X][Y] == 0) iflag = UpDownLeftRight(iflag, X, Y);
                for (Y = numY; Y >= 1; Y--) for (X = numX; X >= 1; X--)
                    if (DT[X][Y] == 0) iflag = UpDownLeftRight(iflag, X, Y);
            }
        }
        protected void onDraw(Canvas cs){			// ===== 描画 ===============
            super.onDraw(cs);
            if(exeMode) display(cs);
            else        endDisplay(cs);
        }
        private void endDisplay(Canvas cs){							// 終了時の表示
            for(int X=1;X<=numX;X++) for (int Y = 1; Y <= numY; Y++)
                if      (DT[X][Y] == Bomb)                    drawBomb(cs, X,Y);
                else if (DT[X][Y] == Hiden0 || DT[X][Y] == 0) drawSpace(cs, X, Y);
                else if (DT[X][Y] < 0)    drawNum(cs, -DT[X][Y], X, Y);
                else                      drawNum(cs,  DT[X][Y], X, Y);
        }
        private void display(Canvas cs){							// ゲーム時の表示
            for (int X = 1; X <= numX; X++) for (int Y=1; Y <= numY; Y++)
                if      (DT[X][Y] < 0) drawHide(cs, X, Y);
                else if (DT[X][Y] > 0) drawNum(cs, DT[X][Y], X, Y);
                else                   drawSpace(cs, X, Y);
        }
        private void drawNum(Canvas cs, int V, int X, int Y){		// 爆弾個数の表示
            float XX = (float)(X * SZ), YY = (float)(Y * SZ);
            drawFlat(cs, bGreen, X, Y);
            cs.drawText(""+ V,XX+SZ/4,YY+(SZ-SZ/4),pString);
        }
        private void drawFlat(Canvas cs, Paint b, int X, int Y) {	//フラットなセルの表示
            float XX = (float)(X * SZ), YY = (float)(Y * SZ);
            cs.drawRect(XX, YY, XX+SZ, YY+SZ, b);
            cs.drawRect(XX, YY, XX+SZ, YY+SZ, pBlack);
        }
        private void drawBomb(Canvas cs, int X, int Y){				// 爆弾の表示
            if (AX == X && AY == Y) drawFlat(cs, bRed, X, Y);
            else                    drawFlat(cs, bYellow, X, Y);
            float XX = (float)(X * SZ), YY = (float)(Y * SZ);
            cs.drawCircle(XX+SZ/2,YY+SZ/2,SZ/4, bBlack);
            cs.drawLine(XX+SZ/2, YY+SZ/2, XX+SZ-5, YY + SZ-25,pBlack2);
        }
        private void drawSpace(Canvas cs, int X, int Y){			// 空白の表示（窪んだセル)
            float XX = (float)(X * SZ), YY = (float)(Y * SZ);
            cs.drawRect(XX, YY, XX+SZ, YY+SZ, bLYellow);
            cs.drawRect(XX, YY, XX+SZ, YY+SZ, pBlack);
            cs.drawLine(XX + 1, YY + 1, XX + SZ-1, YY + 1,pGray);
            cs.drawLine(XX + 2, YY + 2, XX + SZ-2, YY + 2,pGray);
            cs.drawLine(XX + 1, YY + 1, XX + 1, YY + SZ-1,pLGray);
            cs.drawLine(XX + 2, YY + 2, XX + 2, YY + SZ-2,pLGray);
            cs.drawLine(XX + 1, YY + SZ-1, XX + SZ-1, YY + SZ-1,pWhite);
            cs.drawLine(XX+2, YY+SZ-2, XX+SZ-2, YY + SZ-2,pWhite);
            cs.drawLine(XX + SZ-1, YY + 1, XX + SZ-1, YY + SZ-1,pWhite);
            cs.drawLine(XX + SZ-2, YY + 2, XX + SZ-2, YY + SZ-2,pWhite);
        }
        private void drawHide(Canvas cs,int X, int Y){				// 隠された状態の表示
            float XX =(float)(X*SZ), YY = (float)(Y * SZ);
            cs.drawRect(XX, YY,  XX + SZ, YY + SZ,bLGray);
            cs.drawLine(XX     , YY     , XX + SZ, YY     ,pWhite);
            cs.drawLine(XX     , YY     , XX     , YY + SZ,pWhite);
            cs.drawLine(XX     , YY + SZ, XX + SZ, YY + SZ,pBlack);
            cs.drawLine(XX + SZ, YY     , XX + SZ, YY + SZ,pBlack);
            cs.drawLine(XX     , YY + SZ-1, XX + SZ-1, YY + SZ-1,pGray);
            cs.drawLine(XX + SZ-1, YY     , XX + SZ-1, YY + SZ-1,pGray);
            cs.drawLine(XX +  1, YY + SZ-2, XX + SZ-2, YY + SZ-2,pGray);
            cs.drawLine(XX + SZ-2, YY +  1, XX + SZ-2, YY + SZ-2,pGray);
        }
    }
}