package com.wzwosprey.mobiketagsgdxdemo;


import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;

import java.util.Random;

public class Mobike {

    public static final String TAG = Mobike.class.getSimpleName();
    /**
     * World掌管Box2d创建所有物理实体，动态模拟，异步查询。也包含有效的内存管理工具
     */
    private World world;
    private float dt = 1f / 60f;
    private int velocityIterations = 3;
    private int positionIterations = 10;
    private float friction = 0.3f, density = 0.5f, restitution = 0.3f, ratio = 50;
    private int width, height;
    private boolean enable = true;
    private final Random random = new Random();

    private ViewGroup mViewgroup;

    public Mobike(ViewGroup viewgroup) {
        this.mViewgroup = viewgroup;
        density = viewgroup.getContext().getResources().getDisplayMetrics().density;
    }

    public void onSizeChanged(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void onLayout(boolean changed) {
        createWorld(changed);
    }

    public void onDraw(Canvas canvas) {
        if (!enable) {//设置标记，在界面可见的时候开始draw，在界面不可见的时候停止draw
            return;
        }
        //dt 更新引擎的间隔时间
        //velocityIterations 计算速度
        //positionIterations 迭代的次数
        world.step(dt, velocityIterations, positionIterations);
        int childCount = mViewgroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = mViewgroup.getChildAt(i);
            Body body = (Body) view.getTag(R.id.mobike_body_tag);
            if (body != null) {
                //从view中获取绑定的刚体，取出参数，开始更新view
                view.setX(metersToPixels(body.getPosition().x) - view.getWidth() / 2);
                view.setY(metersToPixels(body.getPosition().y) - view.getHeight() / 2);
                view.setRotation(radiansToDegrees(body.getAngle() % 360));
            }
        }
        //手动调用，反复执行draw方法
        mViewgroup.invalidate();
    }

    public void onStart() {
        setEnable(true);
    }

    public void onStop() {
        setEnable(false);
    }

    public void update() {
        world = null;
        onLayout(true);
    }

    private void createWorld(boolean changed) {
        if (world == null) {
            // 在X轴方向上受力为0， 在Y轴方向上受到向下的重力 9.8
            world = new World(new Vector2(0.0f, 9.8f), true);
            //创建边界，注意边界为static静态的，当物体触碰到边界，停止模拟该物体
            createTopAndBottomBounds();
            createLeftAndRightBounds();
        }
        int childCount = mViewgroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = mViewgroup.getChildAt(i);
            Body body = (Body) view.getTag(R.id.mobike_body_tag);
            if (body == null || changed) {
                createBody(world, view);
            }
        }
    }

    /**
     * 创建刚体
     * @param world
     * @param view
     */
    private void createBody(World world, View view) {
        BodyDef bodyDef = new BodyDef();
        //创建刚体描述，因为刚体需要随重力运动，这里type设置为DYNAMIC
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        bodyDef.position.set(pixelsToMeters(view.getX() + view.getWidth() / 2),
                pixelsToMeters(view.getY() + view.getHeight() / 2));
        Shape shape = null;
        Boolean isCircle = (Boolean) view.getTag(R.id.mobike_view_circle_tag);
        if (isCircle != null && isCircle) {
            shape = createCircleShape(view);
        } else {
            shape = createPolygonShape(view);
        }
        //初始化物体信息
        //friction  物体摩擦力
        //restitution 物体恢复系数
        //density 物体密度
        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.friction = friction;
        fixture.restitution = restitution;
        fixture.density = density;

        Body body = world.createBody(bodyDef);
        body.createFixture(fixture);
        view.setTag(R.id.mobike_body_tag, body);
        //初始化物体的运动行为
        body.setLinearVelocity(new Vector2(random.nextFloat(), random.nextFloat()));
    }

    /**
     *创建圆形
     */
    private Shape createCircleShape(View view) {
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(pixelsToMeters(view.getWidth() / 2));
        return circleShape;
    }

    /**
     * 创建多边形
     */
    private Shape createPolygonShape(View view) {
        PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox(pixelsToMeters(view.getWidth() / 2), pixelsToMeters(view.getHeight() / 2));
        return polygonShape;
    }

    /**
     * 创建上边界
     */
    private void createTopAndBottomBounds() {
        /** BodyDef 定义创建刚体所需要的全部数据。可以被重复使用创建不同刚体，BodyDef之后需要绑定Shape **/
        BodyDef bodyDef = new BodyDef();
        /** 动态刚体，受力之后运动会发生改变。 默认创建的是静态BodyType.StaticBody **/
        bodyDef.type = BodyDef.BodyType.StaticBody;

        PolygonShape box = new PolygonShape();
        float boxWidth = pixelsToMeters(width);
        float boxHeight = pixelsToMeters(ratio);
        box.setAsBox(boxWidth, boxHeight);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.3f;
        fixtureDef.restitution = 0.5f;

        bodyDef.position.set(0, -boxHeight);
        Body topBody = world.createBody(bodyDef);
        topBody.createFixture(fixtureDef);

        bodyDef.position.set(0, pixelsToMeters(height) + boxHeight);
        Body bottomBody = world.createBody(bodyDef);
        bottomBody.createFixture(fixtureDef);
    }
    /**
     * 创建下边界
     */
    private void createLeftAndRightBounds() {
        float boxWidth = pixelsToMeters(ratio);
        float boxHeight = pixelsToMeters(height);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        PolygonShape box = new PolygonShape();
        box.setAsBox(boxWidth, boxHeight);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.3f;
        fixtureDef.restitution = 0.5f;

        bodyDef.position.set(-boxWidth, boxHeight);
        Body leftBody = world.createBody(bodyDef);
        leftBody.createFixture(fixtureDef);


        bodyDef.position.set(pixelsToMeters(width) + boxWidth, 0);
        Body rightBody = world.createBody(bodyDef);
        rightBody.createFixture(fixtureDef);
    }

    private float radiansToDegrees(float radians) {
        return radians / 3.14f * 180f;
    }

    private float degreesToRadians(float degrees) {
        return (degrees / 180f) * 3.14f;
    }

    public float metersToPixels(float meters) {
        return meters * ratio;
    }

    public float pixelsToMeters(float pixels) {
        return pixels / ratio;
    }

    /**
     * 传感器变化
     * @param x
     * @param y
     */
    public void onSensorChanged(float x, float y) {
        int childCount = mViewgroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            Vector2 impulse = new Vector2(x, y);
            View view = mViewgroup.getChildAt(i);
            Body body = (Body) view.getTag(R.id.mobike_body_tag);
            if (body != null) {
                body.applyLinearImpulse(impulse, body.getPosition(), true);
            }
        }
    }

    public float getFriction() {
        return friction;
    }

    public void setFriction(float friction) {
        if (friction >= 0) {
            this.friction = friction;
        }
    }

    public float getDensity() {
        return density;
    }

    public void setDensity(float density) {
        if (density >= 0) {
            this.density = density;
        }
    }

    public float getRestitution() {
        return restitution;
    }

    public void setRestitution(float restitution) {
        if (restitution >= 0) {
            this.restitution = restitution;
        }
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        if (ratio >= 0) {
            this.ratio = ratio;
        }
    }

    public boolean getEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
        mViewgroup.invalidate();
    }
}
